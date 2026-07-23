package com.buzzanalysis.application.post;

import com.buzzanalysis.application.post.dto.BuzzScoreHistoryPointDto;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.score.BuzzScoreHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** 投稿のBuzzScore推移(履歴)を取得するユースケース。 */
@Service
public class BuzzScoreHistoryApplicationService {

    private final PostRepository postRepository;
    private final BuzzScoreHistoryRepository buzzScoreHistoryRepository;

    public BuzzScoreHistoryApplicationService(PostRepository postRepository,
                                               BuzzScoreHistoryRepository buzzScoreHistoryRepository) {
        this.postRepository = postRepository;
        this.buzzScoreHistoryRepository = buzzScoreHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<BuzzScoreHistoryPointDto> getHistory(UUID postId) {
        if (postRepository.findById(postId).isEmpty()) {
            throw EntityNotFoundException.of("Post", postId);
        }
        return buzzScoreHistoryRepository.findByPostIdOrderByCalculatedAtAsc(postId).stream()
                .map(BuzzScoreHistoryPointDto::from)
                .toList();
    }
}
