// バックエンドのDataModeResponseに対応。SNS公式APIキーが未設定のプラットフォームは
// 疑似(デモ)データにフォールバックするため、フロントエンドはこれを見てユーザーに明示する。
export interface DataMode {
  instagramLive: boolean;
  tiktokLive: boolean;
  xLive: boolean;
  anyPlatformLive: boolean;
}
