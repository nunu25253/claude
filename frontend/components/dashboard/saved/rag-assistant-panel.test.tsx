import { afterEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import { RagAssistantPanel } from "./rag-assistant-panel";
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

vi.mock("@/lib/hooks/use-rag", () => ({
  useQueryRagAssistant: () => mutationState,
}));

vi.mock("@/lib/auth/auth-context", () => ({
  useAuth: () => ({ user: { id: "user-1", email: "user@example.com", displayName: "Test User", emailVerified: true } }),
}));

function savedItem(id: string): SavedAnalysis {
  return {
    id: `saved-${id}`,
    createdAt: "2026-07-01T00:00:00Z",
    post: {
      id,
      url: `https://www.instagram.com/p/${id}/`,
      platform: "INSTAGRAM",
      caption: "投稿",
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

describe("RagAssistantPanel", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("renders nothing when there are no saved items", () => {
    mutationState = { mutate: mutateMock, isPending: false, isError: false, isSuccess: false, error: null, data: null };
    const { container } = render(<RagAssistantPanel items={[]} />);

    expect(container).toBeEmptyDOMElement();
  });

  it("disables the ask button until a question is entered", () => {
    mutationState = { mutate: mutateMock, isPending: false, isError: false, isSuccess: false, error: null, data: null };
    render(<RagAssistantPanel items={[savedItem("post-1")]} />);

    const button = screen.getByRole("button", { name: "質問する" });
    expect(button).toBeDisabled();

    fireEvent.change(screen.getByPlaceholderText("保存した投稿について質問する"), { target: { value: "質問文" } });
    expect(button).toBeEnabled();
  });

  it("calls the mutation with the entered question", () => {
    mutationState = { mutate: mutateMock, isPending: false, isError: false, isSuccess: false, error: null, data: null };
    render(<RagAssistantPanel items={[savedItem("post-1")]} />);

    fireEvent.change(screen.getByPlaceholderText("保存した投稿について質問する"), {
      target: { value: "一番CTAが強かった投稿は？" },
    });
    fireEvent.click(screen.getByRole("button", { name: "質問する" }));

    expect(mutateMock).toHaveBeenCalledWith({ question: "一番CTAが強かった投稿は？" });
  });

  it("shows an error state when the mutation fails", () => {
    mutationState = {
      mutate: mutateMock,
      isPending: false,
      isError: true,
      isSuccess: false,
      error: new ApiError(400, { message: "質問文を入力してください。" }),
      data: null,
    };
    render(<RagAssistantPanel items={[savedItem("post-1")]} />);

    expect(screen.getByText("質問文を入力してください。")).toBeInTheDocument();
  });

  it("renders the answer and sources on success", () => {
    mutationState = {
      mutate: mutateMock,
      isPending: false,
      isError: false,
      isSuccess: true,
      error: null,
      data: {
        answer: "最もCTAが強かったのは1件目の投稿です。",
        sources: [
          {
            id: "doc-1",
            sourceType: "ANALYSIS_RESULT",
            sourceId: "post-1",
            contentText: "CTA: プロフィールへの誘導が明確",
            createdAt: "2026-07-01T00:00:00Z",
          },
        ],
      },
    };
    render(<RagAssistantPanel items={[savedItem("post-1")]} />);

    expect(screen.getByText("最もCTAが強かったのは1件目の投稿です。")).toBeInTheDocument();
    expect(screen.getByText("投稿分析結果")).toBeInTheDocument();
    expect(screen.getByText(/CTA: プロフィールへの誘導が明確/)).toBeInTheDocument();
  });
});
