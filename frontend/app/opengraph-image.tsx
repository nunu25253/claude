import { ImageResponse } from "next/og";

export const size = { width: 1200, height: 630 };
export const contentType = "image/png";

// next/og(Satori)のデフォルトフォントはCJKグリフを含まないため、画像内テキストは
// 英語のみにする(日本語タイトル/説明はog:title/og:descriptionのメタ文字列側で正しく表示される)。
export default function OpengraphImage() {
  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          background: "linear-gradient(135deg, #4a63f5 0%, #1f2568 100%)",
          color: "#ffffff",
        }}
      >
        <div
          style={{
            display: "flex",
            width: 140,
            height: 140,
            alignItems: "center",
            justifyContent: "center",
            borderRadius: 32,
            background: "rgba(255, 255, 255, 0.15)",
          }}
        >
          <svg width="72" height="72" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M17.5 6 9 18h6l-1 8 8.5-12h-6l1-8z" fill="#ffffff" />
          </svg>
        </div>
        <div style={{ fontSize: 64, fontWeight: 700, marginTop: 24 }}>SNS AI Buzz Analysis</div>
        <div style={{ fontSize: 30, opacity: 0.85, marginTop: 12 }}>
          AI-powered social post analytics
        </div>
      </div>
    ),
    size,
  );
}
