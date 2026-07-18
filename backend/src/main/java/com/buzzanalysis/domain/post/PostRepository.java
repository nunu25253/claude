package com.buzzanalysis.domain.post;

import com.buzzanalysis.domain.platform.Platform;

import java.util.Optional;
import java.util.UUID;

/**
 * Post集約のリポジトリインターフェース。検索系は {@link PostSearchCriteria} を介して
 * infrastructure層（Spring Data JPA + Specification等）にページング/ソートを委譲する。
 */
public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(UUID id);

    Optional<Post> findByPlatformAndExternalId(Platform platform, String externalId);

    Optional<Post> findByUrl(String url);

    PostSearchResult search(PostSearchCriteria criteria);
}
