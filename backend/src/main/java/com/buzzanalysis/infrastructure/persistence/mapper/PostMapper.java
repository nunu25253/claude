package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.infrastructure.persistence.entity.PostEntity;
import org.springframework.stereotype.Component;

/** {@link Post}（ドメイン）と {@link PostEntity}（JPA）の相互変換を行う。 */
@Component
public class PostMapper {

    private final PlatformMapper platformMapper;

    public PostMapper(PlatformMapper platformMapper) {
        this.platformMapper = platformMapper;
    }

    public PostEntity toEntity(Post post) {
        return new PostEntity(
                post.getId(), post.getSocialAccountId(), platformMapper.toEntity(post.getPlatform()),
                post.getExternalId(), post.getUrl(), post.getPublishedAt(), post.getAuthorName(), post.getCaption(),
                post.getHashtags(), post.getLikeCount(), post.getCommentCount(), post.getViewCount(),
                post.getShareCount(), post.getVideoDurationSeconds(), post.getImageCount(),
                toEntityType(post.getPostType()), post.getCreatedAt(), post.getUpdatedAt()
        );
    }

    public Post toDomain(PostEntity entity) {
        return new Post(
                entity.getId(), entity.getSocialAccountId(), platformMapper.toDomain(entity.getPlatform()),
                entity.getExternalId(), entity.getUrl(), entity.getPublishedAt(), entity.getAuthorName(),
                entity.getCaption(), entity.getHashtags(), entity.getLikeCount(), entity.getCommentCount(),
                entity.getViewCount(), entity.getShareCount(), entity.getVideoDurationSeconds(),
                entity.getImageCount(), toDomainType(entity.getPostType()), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    private PostEntity.PostTypeEnum toEntityType(PostType type) {
        return type == null ? null : PostEntity.PostTypeEnum.valueOf(type.name());
    }

    private PostType toDomainType(PostEntity.PostTypeEnum type) {
        return type == null ? null : PostType.valueOf(type.name());
    }
}
