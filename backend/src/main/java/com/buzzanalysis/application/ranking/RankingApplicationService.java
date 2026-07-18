package com.buzzanalysis.application.ranking;

import com.buzzanalysis.application.post.dto.PostDto;
import com.buzzanalysis.application.ranking.dto.RankingEntryDto;
import com.buzzanalysis.application.ranking.dto.RankingQuery;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.ranking.Ranking;
import com.buzzanalysis.domain.ranking.RankingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ランキング取得ユースケース（急上昇/週間/月間、ジャンル別、SNS別）。
 */
@Service
public class RankingApplicationService {

    private final RankingRepository rankingRepository;
    private final PostRepository postRepository;

    public RankingApplicationService(RankingRepository rankingRepository, PostRepository postRepository) {
        this.rankingRepository = rankingRepository;
        this.postRepository = postRepository;
    }

    @Cacheable(value = "rankings", key = "#query.type() + ':' + #query.genre() + ':' + #query.platform() + ':' + #query.limit()")
    @Transactional(readOnly = true)
    public List<RankingEntryDto> getRankings(RankingQuery query) {
        List<Ranking> rankings = rankingRepository.findByFilters(query.type(), query.genre(), query.platform(), query.limit());
        return rankings.stream()
                .map(this::toDto)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private RankingEntryDto toDto(Ranking ranking) {
        Optional<Post> post = postRepository.findById(ranking.getPostId());
        if (post.isEmpty()) {
            return null;
        }
        return new RankingEntryDto(ranking.getRankPosition(), ranking.getScore(), PostDto.from(post.get()));
    }
}
