package com.buzzanalysis.infrastructure.external.platform;

import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.platform.PlatformFactory;
import com.buzzanalysis.domain.platform.SocialPlatform;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * {@link PlatformFactory} の実装（Factoryパターン）。Spring DIで注入された全 {@link SocialPlatform} Beanを
 * プラットフォーム種別ごとにディスパッチする。新しいSNSを追加する場合は、対応する {@link SocialPlatform} 実装を
 * Beanとして登録するだけで、このFactoryが自動的に認識する。
 */
@Component
public class PlatformFactoryImpl implements PlatformFactory {

    private final Map<Platform, SocialPlatform> implementationsByPlatform;

    public PlatformFactoryImpl(List<SocialPlatform> implementations) {
        this.implementationsByPlatform = new EnumMap<>(Platform.class);
        for (SocialPlatform impl : implementations) {
            implementationsByPlatform.put(impl.platform(), impl);
        }
    }

    @Override
    public SocialPlatform resolve(Platform platform) {
        SocialPlatform impl = implementationsByPlatform.get(platform);
        if (impl == null) {
            throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
        return impl;
    }

    @Override
    public Platform detectPlatformFromUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BusinessRuleViolationException("Post URL must not be blank");
        }
        String lower = url.toLowerCase();
        if (lower.contains("instagram.com")) {
            return Platform.INSTAGRAM;
        }
        if (lower.contains("tiktok.com")) {
            return Platform.TIKTOK;
        }
        if (lower.contains("x.com") || lower.contains("twitter.com")) {
            return Platform.X;
        }
        if (lower.contains("youtube.com") || lower.contains("youtu.be")) {
            return Platform.YOUTUBE;
        }
        if (lower.contains("pinterest.com")) {
            return Platform.PINTEREST;
        }
        if (lower.contains("threads.net")) {
            return Platform.THREADS;
        }
        throw new IllegalArgumentException("Could not detect a supported platform from URL: " + url);
    }
}
