import { afterEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { ErrorState } from "./error-state";
import { ApiError } from "@/lib/types/common";
import { authApi } from "@/lib/api";

const testUser = { id: "user-1", email: "user@example.com", displayName: "Test User", emailVerified: false };

vi.mock("@/lib/auth/auth-context", () => ({
  useAuth: () => ({ user: testUser }),
}));

vi.mock("@/lib/api", () => ({
  authApi: {
    resendEmailVerification: vi.fn(),
  },
}));

describe("ErrorState", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("shows a generic message for non-ApiError values", () => {
    render(<ErrorState error={new Error("boom")} />);

    expect(screen.getByText("データの取得に失敗しました")).toBeInTheDocument();
    expect(screen.getByText("予期しないエラーが発生しました。")).toBeInTheDocument();
  });

  it("shows a dedicated offline message when ApiError.status is 0", () => {
    render(<ErrorState error={new ApiError(0, { message: "サーバーに接続できませんでした。" })} />);

    expect(screen.getByText("APIサーバーに接続できません")).toBeInTheDocument();
  });

  it("calls onRetry when the retry button is clicked for a generic error", () => {
    const onRetry = vi.fn();
    render(<ErrorState error={new Error("boom")} onRetry={onRetry} />);

    screen.getByRole("button", { name: "再試行" }).click();
    expect(onRetry).toHaveBeenCalledTimes(1);
  });

  it("shows an upsell CTA linking to /billing when the AI usage quota is exceeded", () => {
    const onRetry = vi.fn();
    render(
      <ErrorState
        error={
          new ApiError(400, {
            message: "本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。",
            code: "AI_USAGE_QUOTA_EXCEEDED",
          })
        }
        onRetry={onRetry}
      />,
    );

    expect(screen.getByText("本日のAI機能の利用回数上限に達しました")).toBeInTheDocument();
    expect(screen.getByText(/明日以降に再度お試しください/)).toBeInTheDocument();

    const upgradeLink = screen.getByRole("link", { name: "プランを見る" });
    expect(upgradeLink).toHaveAttribute("href", "/billing");

    // 上限超過はリトライしても同じ日は解消しないため、再試行ボタンは出さない
    expect(screen.queryByRole("button", { name: "再試行" })).not.toBeInTheDocument();
  });

  it("does not show the upsell CTA for a generic 400 error with a different code", () => {
    render(
      <ErrorState error={new ApiError(400, { message: "不正なリクエストです。", code: "VALIDATION_ERROR" })} />,
    );

    expect(screen.queryByRole("link", { name: "プランを見る" })).not.toBeInTheDocument();
    expect(screen.getByText("不正なリクエストです。")).toBeInTheDocument();
  });

  it("shows a resend-verification CTA when the free trial analysis is already used while unverified", async () => {
    vi.mocked(authApi.resendEmailVerification).mockResolvedValue(undefined);
    render(
      <ErrorState
        error={
          new ApiError(400, {
            message: "無料お試し分析は既にご利用いただきました。",
            code: "EMAIL_NOT_VERIFIED",
          })
        }
      />,
    );

    expect(screen.getByText("無料お試し分析は既にご利用いただきました")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "確認メールを再送する" }));

    await waitFor(() => expect(authApi.resendEmailVerification).toHaveBeenCalledWith({ email: "user@example.com" }));
    expect(await screen.findByText("確認メールを再送しました")).toBeInTheDocument();
  });
});
