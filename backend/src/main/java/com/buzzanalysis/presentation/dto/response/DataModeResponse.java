package com.buzzanalysis.presentation.dto.response;

/**
 * 各SNSプラットフォームの公式APIキーが実際に設定されているかを表すレスポンス。
 * 未設定のプラットフォームは投稿データ取得時にシミュレーション(疑似)データへフォールバックするため、
 * フロントエンドはこれを見て「表示中のデータはデモである」旨をユーザーに明示する
 * (レビューで指摘された、実データとデモデータの区別がつかない問題への対応)。
 */
public record DataModeResponse(
        boolean instagramLive,
        boolean tiktokLive,
        boolean xLive,
        boolean anyPlatformLive
) {
    public static DataModeResponse of(boolean instagramLive, boolean tiktokLive, boolean xLive) {
        return new DataModeResponse(instagramLive, tiktokLive, xLive, instagramLive || tiktokLive || xLive);
    }
}
