import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { ScoreComparisonSection } from "./score-comparison-section";

const useScoreComparisonMock = vi.fn();

vi.mock("@/lib/hooks/use-posts", () => ({
  useScoreComparison: (...args: unknown[]) => useScoreComparisonMock(...args),
}));

describe("ScoreComparisonSection", () => {
  it("renders nothing while loading", () => {
    useScoreComparisonMock.mockReturnValue({ data: undefined, isLoading: true, isError: false });
    const { container } = render(<ScoreComparisonSection postId="post-1" currentScore={80} />);

    expect(container).toBeEmptyDOMElement();
  });

  it("renders nothing when neither comparison is available", () => {
    useScoreComparisonMock.mockReturnValue({
      data: { previousPostScore: null, genreAverageScore: null, genreSampleSize: 0 },
      isLoading: false,
      isError: false,
    });
    const { container } = render(<ScoreComparisonSection postId="post-1" currentScore={80} />);

    expect(container).toBeEmptyDOMElement();
  });

  it("shows an upward badge when the current score is higher than the previous post", () => {
    useScoreComparisonMock.mockReturnValue({
      data: { previousPostScore: 60, genreAverageScore: null, genreSampleSize: 0 },
      isLoading: false,
      isError: false,
    });
    render(<ScoreComparisonSection postId="post-1" currentScore={80} />);

    expect(screen.getByText("前回投稿比")).toBeInTheDocument();
    expect(screen.getByText("+20.0")).toBeInTheDocument();
  });

  it("shows a downward badge when the current score is lower than the genre average", () => {
    useScoreComparisonMock.mockReturnValue({
      data: { previousPostScore: null, genreAverageScore: 90, genreSampleSize: 12 },
      isLoading: false,
      isError: false,
    });
    render(<ScoreComparisonSection postId="post-1" currentScore={80} />);

    expect(screen.getByText("同ジャンル平均比(12件)")).toBeInTheDocument();
    expect(screen.getByText("-10.0")).toBeInTheDocument();
  });

  it("shows both badges when both comparisons are available", () => {
    useScoreComparisonMock.mockReturnValue({
      data: { previousPostScore: 80, genreAverageScore: 80, genreSampleSize: 5 },
      isLoading: false,
      isError: false,
    });
    render(<ScoreComparisonSection postId="post-1" currentScore={80} />);

    expect(screen.getByText("前回投稿比")).toBeInTheDocument();
    expect(screen.getByText("同ジャンル平均比(5件)")).toBeInTheDocument();
    expect(screen.getAllByText("±0")).toHaveLength(2);
  });
});
