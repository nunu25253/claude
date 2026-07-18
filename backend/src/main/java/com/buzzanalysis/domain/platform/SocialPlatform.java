package com.buzzanalysis.domain.platform;

import java.util.Optional;

/**
 * SNSプラットフォームごとのデータ取得を抽象化するドメインインターフェース（Strategy/Adapter）。
 * Instagram/TikTok/X等、各SNSの公式APIクライアントはこのインターフェースを実装する。
 * 新しいSNSを追加する場合は、この interface を実装したクラスを infrastructure 層に追加し、
 * {@link Platform} に定数を追加して PlatformFactory に登録するだけでよい。
 *
 * <p>公開APIで取得できる情報のみを扱う。インプレッション・リーチ・保存数などの非公開指標は対象外。</p>
 */
public interface SocialPlatform {

    /**
     * このクライアントが担当するプラットフォーム種別を返す。
     */
    Platform platform();

    /**
     * 投稿の公開URLまたは外部投稿IDから、公開されている投稿データを取得する。
     *
     * @param postUrlOrId 投稿のURL、もしくはプラットフォーム固有の投稿ID
     * @return 取得できた場合は投稿データ、存在しない/非公開の場合は empty
     */
    Optional<FetchedPostData> fetchPost(String postUrlOrId);

    /**
     * アカウントのユーザー名または外部アカウントIDから、公開プロフィール情報を取得する。
     *
     * @param usernameOrId ユーザー名、もしくはプラットフォーム固有のアカウントID
     * @return 取得できた場合はアカウントデータ、存在しない場合は empty
     */
    Optional<FetchedAccountData> fetchAccount(String usernameOrId);
}
