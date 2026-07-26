package com.buzzanalysis.domain.platform;

/**
 * プラットフォーム種別から対応する {@link SocialPlatform} 実装を解決するFactory（Factoryパターン）。
 * 実装はinfrastructure層に置き、Instagram/TikTok/X等の各実装Beanを内部で保持・ディスパッチする。
 */
public interface PlatformFactory {

    /**
     * 指定されたプラットフォームに対応する実装を返す。
     *
     * @throws IllegalArgumentException 未対応のプラットフォームが指定された場合
     */
    SocialPlatform resolve(Platform platform);

    /**
     * 投稿URLの文字列からプラットフォームを推定する（例: instagram.com -> INSTAGRAM）。
     *
     * @throws IllegalArgumentException URLからプラットフォームを判定できない場合
     */
    Platform detectPlatformFromUrl(String url);
}
