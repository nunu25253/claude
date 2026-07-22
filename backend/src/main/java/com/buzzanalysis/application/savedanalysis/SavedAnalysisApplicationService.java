package com.buzzanalysis.application.savedanalysis;

import com.buzzanalysis.application.post.dto.AnalysisResultDto;
import com.buzzanalysis.application.post.dto.BuzzScoreDto;
import com.buzzanalysis.application.post.dto.PostDto;
import com.buzzanalysis.application.savedanalysis.dto.SaveAnalysisCommand;
import com.buzzanalysis.application.savedanalysis.dto.SavedAnalysisDetailDto;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 保存済み分析（ブックマーク）ユースケース。
 * {@link SavedAnalysis}自体は投稿ID(postId)のみを保持するブックマークだが、フロントエンドが
 * 分析結果画面をそのまま表示できるよう、一覧・保存のレスポンスには投稿本体・AI分析結果・BuzzScoreを
 * 都度リポジトリから引いて合成する（{@code POST /posts/analyze}のレスポンスと同じ形に揃える）。
 */
@Service
public class SavedAnalysisApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SavedAnalysisApplicationService.class);

    private final SavedAnalysisRepository savedAnalysisRepository;
    private final PostRepository postRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final BuzzScoreRepository buzzScoreRepository;

    public SavedAnalysisApplicationService(SavedAnalysisRepository savedAnalysisRepository,
                                            PostRepository postRepository,
                                            AnalysisResultRepository analysisResultRepository,
                                            BuzzScoreRepository buzzScoreRepository) {
        this.savedAnalysisRepository = savedAnalysisRepository;
        this.postRepository = postRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.buzzScoreRepository = buzzScoreRepository;
    }

    /**
     * 保存済み分析一覧を返す。件数分のfindByIdをループで叩くN+1クエリになっていたため、
     * 投稿ID一覧をまとめてバッチ取得しメモリ上で結合する（保存件数によらずクエリ回数は3回で済む）。
     */
    @Transactional(readOnly = true)
    public List<SavedAnalysisDetailDto> list(UUID userId) {
        List<SavedAnalysis> savedList = savedAnalysisRepository.findByUserId(userId);
        List<UUID> postIds = savedList.stream().map(SavedAnalysis::getPostId).distinct().toList();

        Map<UUID, Post> postsByPostId = postRepository.findByIdIn(postIds).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        Map<UUID, AnalysisResultDto> analysesByPostId = analysisResultRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(a -> a.getPostId(), AnalysisResultDto::from));
        Map<UUID, BuzzScoreDto> buzzScoresByPostId = buzzScoreRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(b -> b.getPostId(), BuzzScoreDto::from));

        return savedList.stream()
                .filter(saved -> {
                    boolean exists = postsByPostId.containsKey(saved.getPostId());
                    if (!exists) {
                        log.warn("Saved analysis {} references a post that no longer exists: postId={}",
                                saved.getId(), saved.getPostId());
                    }
                    return exists;
                })
                .map(saved -> new SavedAnalysisDetailDto(
                        saved.getId(), saved.getNote(), saved.getCreatedAt(),
                        PostDto.from(postsByPostId.get(saved.getPostId())),
                        analysesByPostId.get(saved.getPostId()),
                        buzzScoresByPostId.get(saved.getPostId())))
                .toList();
    }

    @Transactional
    public SavedAnalysisDetailDto save(SaveAnalysisCommand command) {
        postRepository.findById(command.postId())
                .orElseThrow(() -> EntityNotFoundException.of("Post", command.postId()));
        SavedAnalysis entity = SavedAnalysis.createNew(command.userId(), command.postId(), command.note());
        SavedAnalysis saved = savedAnalysisRepository.save(entity);
        return enrich(saved).orElseThrow(() -> EntityNotFoundException.of("Post", command.postId()));
    }

    @Transactional
    public void delete(UUID id, UUID requestingUserId) {
        SavedAnalysis existing = savedAnalysisRepository.findById(id)
                .orElseThrow(() -> EntityNotFoundException.of("SavedAnalysis", id));
        if (!existing.getUserId().equals(requestingUserId)) {
            throw new BusinessRuleViolationException("You are not allowed to delete this saved analysis");
        }
        savedAnalysisRepository.deleteById(id);
    }

    /** 投稿が削除済みで参照できない場合はempty（一覧からは除外し、保存直後ならエラーとして扱う）。 */
    private Optional<SavedAnalysisDetailDto> enrich(SavedAnalysis saved) {
        Optional<Post> post = postRepository.findById(saved.getPostId());
        if (post.isEmpty()) {
            log.warn("Saved analysis {} references a post that no longer exists: postId={}",
                    saved.getId(), saved.getPostId());
            return Optional.empty();
        }
        AnalysisResultDto analysisDto = analysisResultRepository.findByPostId(saved.getPostId())
                .map(AnalysisResultDto::from).orElse(null);
        BuzzScoreDto buzzScoreDto = buzzScoreRepository.findByPostId(saved.getPostId())
                .map(BuzzScoreDto::from).orElse(null);
        return Optional.of(new SavedAnalysisDetailDto(
                saved.getId(), saved.getNote(), saved.getCreatedAt(),
                PostDto.from(post.get()), analysisDto, buzzScoreDto));
    }
}
