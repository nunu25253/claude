package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.infrastructure.persistence.entity.PlatformEnum;
import org.springframework.stereotype.Component;

/** ドメインの {@link Platform} と永続化層の {@link PlatformEnum} の相互変換を行う。 */
@Component
public class PlatformMapper {

    public PlatformEnum toEntity(Platform platform) {
        return platform == null ? null : PlatformEnum.valueOf(platform.name());
    }

    public Platform toDomain(PlatformEnum platformEnum) {
        return platformEnum == null ? null : Platform.valueOf(platformEnum.name());
    }
}
