import { afterEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import { CommonalityPanel } from "./commonality-panel";
import { ApiError } from "@/lib/types/common";
import type { SavedAnalysis } from "@/lib/types";

const mutateMock = vi.fn();
let mutationState: {
  mutate: typeof mutateMock;
  isPending: boolean;
  isError: boolean;
  isSuccess: boolean;
  error: unknown;
  data: unknown;
};

vi.mock("@/lib/hooks/use-commonality", () => ({
  useAnalyzeCommonality: () => mutationState,
}));

vi.mock("@/lib/auth/auth-context", () => ({
  useAuth: () => ({ user: { id: "user-1", email: "user@example.com", displayName: "Test User", emailVerified: true } }),
}));

function savedItem(id: string, caption: string): SavedAnalysis {
  return {
    id: `saved-${id}`,
    createdAt: "2026-07-01T00:00:00Z",
    post: {
      id,
      url: `https://www.instagram.com/p/${id}/`,
      platform: "INSTAGRAM",
      caption,
      hashtags: [],
      likeCount: 10,
      commentCount: 1,
      shareCount: 0,
    },
    analysis: null,
    buzzScore: null,
    alertThreshold: null,
    alertTriggeredAt: null,
  };
}

describe("CommonalityPanel", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("renders nothing when there are no saved items", () => {
    mutationState = { mutate: mutateMock, isPending: false, isError: false, isSuccess: false, error: null, data: null };
    const { container } = render(<CommonalityPanel items={[]} />);

    expect(container).toBeEmptyDOMElement();
  });

  it("disables the analyze button until 2 or more posts are selected", () => {
    mutationState = { mutate: mutateMock, isPending: false, isError: false, isSuccess: false, error: null, data: null };
    render(<CommonalityPanel items={[savedItem("post-1", "投稿A"), savedItem("post-2", "投稿B")]} />);

    const button = screen.getByRole("button", { name: "共通点を分析する" });
    expect(button).toBeDisabled();

    fireEvent.click(screen.getByRole("checkbox", { name: /投稿A/ }));
    expect(button).toBeDisabled();

    fireEvent.click(screen.getByRole("checkbox", { name: /投稿B/ }));
    expect(button).toBeEnabled();
  });

  it("calls the mutation with the selected post IDs", () => {
    mutationState = { mutate: mutateMock, isPending: false, isError: false, isSuccess: false, error: null, data: null };
    render(<CommonalityPanel items={[savedItem("post-1", "投稿A"), savedItem("post-2", "投稿B")]} />);

    fireEvent.click(screen.getByRole("checkbox", { name: /投稿A/ }));
    fireEvent.click(screen.getByRole("checkbox", { name: /投稿B/ }));
    fireEvent.click(screen.getByRole("button", { name: "共通点を分析する" }));

    expect(mutateMock).toHaveBeenCalledWith({ postIds: ["post-1", "post-2"] });
  });

  it("shows an error state when the mutation fails", () => {
    mutationState = {
      mutate: mutateMock,
      isPending: false,
      isError: true,
      isSuccess: false,
      error: new ApiError(400, { message: "投稿IDは100件までです。" }),
      data: null,
    };
    render(<CommonalityPanel items={[savedItem("post-1", "投稿A"), savedItem("post-2", "投稿B")]} />);

    expect(screen.getByText("投稿IDは100件までです。")).toBeInTheDocument();
  });

  it("renders the extracted common patterns on success", () => {
    mutationState = {
      mutate: mutateMock,
      isPending: false,
      isError: false,
      isSuccess: true,
      error: null,
      data: {
        totalPostCount: 3,
        aiSampleSize: 3,
        commonHashtags: ["トレンド", "バズる"],
        commonVideoDurationSeconds: 30,
        commonPostingHour: 20,
        commonContentFormat: "SHORT_VIDEO",
        commonTitlePattern: "疑問形で始まるタイトル",
        commonHookPattern: undefined,
        commonCtaPattern: undefined,
        commonStructurePattern: undefined,
        commonTargetPattern: undefined,
      },
    };
    render(<CommonalityPanel items={[savedItem("post-1", "投稿A"), savedItem("post-2", "投稿B")]} />);

    expect(screen.getByText("#トレンド")).toBeInTheDocument();
    expect(screen.getByText("約30秒")).toBeInTheDocument();
    expect(screen.getByText("20時台")).toBeInTheDocument();
    expect(screen.getByText("短尺動画")).toBeInTheDocument();
    expect(screen.getByText("疑問形で始まるタイトル")).toBeInTheDocument();
  });
});
