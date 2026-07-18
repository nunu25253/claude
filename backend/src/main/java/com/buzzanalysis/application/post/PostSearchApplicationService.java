package com.buzzanalysis.application.post;

import com.buzzanalysis.application.post.dto.PostDto;
import com.buzzanalysis.application.post.dto.PostSearchQuery;
import com.buzzanalysis.application.post.dto.PostSearchResultDto;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.post.PostSearchResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 投稿検索ユースケース（キーワード/ハッシュタグ/アカウント検索、ページング・ソート対応）。
 */
@Service
public class PostSearchApplicationService {

    private final PostRepository postRepository;

    public PostSearchApplicationService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public PostSearchResultDto search(PostSearchQuery query) {
        PostSearchCriteria criteria = new PostSearchCriteria(
                query.keyword(), query.hashtag(), query.accountId(), query.platform(),
                query.page(), query.size(), query.sortBy(), query.ascending()
        );
        PostSearchResult result = postRepository.search(criteria);
        List<PostDto> content = result.content().stream().map(PostDto::from).toList();
        return new PostSearchResultDto(content, result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
