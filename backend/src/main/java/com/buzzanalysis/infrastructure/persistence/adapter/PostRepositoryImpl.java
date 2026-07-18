package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.post.PostSearchResult;
import com.buzzanalysis.infrastructure.persistence.entity.PlatformEnum;
import com.buzzanalysis.infrastructure.persistence.entity.PostEntity;
import com.buzzanalysis.infrastructure.persistence.mapper.PlatformMapper;
import com.buzzanalysis.infrastructure.persistence.mapper.PostMapper;
import com.buzzanalysis.infrastructure.persistence.repository.PostJpaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * {@link PostRepository} のJPA実装（Repositoryパターン）。
 * 検索条件は {@link Specification} を用いて動的にクエリを組み立てる。
 */
@Repository
public class PostRepositoryImpl implements PostRepository {

    /** ソート可能な項目のホワイトリスト。任意の文字列をそのままJPQLに使わないための安全策。 */
    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "publishedAt", "likeCount", "commentCount", "viewCount", "shareCount", "createdAt"
    );

    private final PostJpaRepository jpaRepository;
    private final PostMapper mapper;
    private final PlatformMapper platformMapper;

    public PostRepositoryImpl(PostJpaRepository jpaRepository, PostMapper mapper, PlatformMapper platformMapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.platformMapper = platformMapper;
    }

    @Override
    public Post save(Post post) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(post)));
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Post> findByPlatformAndExternalId(Platform platform, String externalId) {
        return jpaRepository.findByPlatformAndExternalId(platformMapper.toEntity(platform), externalId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Post> findByUrl(String url) {
        return jpaRepository.findByUrl(url).map(mapper::toDomain);
    }

    @Override
    public PostSearchResult search(PostSearchCriteria criteria) {
        Specification<PostEntity> spec = buildSpecification(criteria);
        String sortField = SORTABLE_FIELDS.contains(criteria.sortBy()) ? criteria.sortBy() : "publishedAt";
        Sort sort = Sort.by(criteria.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
        PageRequest pageRequest = PageRequest.of(criteria.page(), criteria.size(), sort);

        var page = jpaRepository.findAll(spec, pageRequest);
        List<Post> content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PostSearchResult(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private Specification<PostEntity> buildSpecification(PostSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
                String likePattern = "%" + criteria.keyword().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("caption")), likePattern));
            }
            if (criteria.hashtag() != null && !criteria.hashtag().isBlank()) {
                if (query != null) {
                    query.distinct(true);
                }
                var hashtagsJoin = root.join("hashtags");
                predicates.add(cb.equal(hashtagsJoin, criteria.hashtag()));
            }
            if (criteria.accountId() != null) {
                predicates.add(cb.equal(root.get("socialAccountId"), criteria.accountId()));
            }
            if (criteria.platform() != null) {
                PlatformEnum platformEnum = platformMapper.toEntity(criteria.platform());
                predicates.add(cb.equal(root.get("platform"), platformEnum));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
