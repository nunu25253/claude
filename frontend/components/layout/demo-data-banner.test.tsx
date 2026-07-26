import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { DemoDataBanner } from "./demo-data-banner";

const useDataModeMock = vi.fn();

vi.mock("@/lib/hooks/use-system", () => ({
  useDataMode: () => useDataModeMock(),
}));

describe("DemoDataBanner", () => {
  it("renders nothing while loading", () => {
    useDataModeMock.mockReturnValue({ data: undefined });
    const { container } = render(<DemoDataBanner />);

    expect(container).toBeEmptyDOMElement();
  });

  it("renders nothing when at least one platform has a live API key configured", () => {
    useDataModeMock.mockReturnValue({
      data: { instagramLive: true, tiktokLive: false, xLive: false, anyPlatformLive: true },
    });
    const { container } = render(<DemoDataBanner />);

    expect(container).toBeEmptyDOMElement();
  });

  it("shows the demo data notice when no platform has a live API key configured", () => {
    useDataModeMock.mockReturnValue({
      data: { instagramLive: false, tiktokLive: false, xLive: false, anyPlatformLive: false },
    });
    render(<DemoDataBanner />);

    expect(screen.getByText(/デモデータで動作しています/)).toBeInTheDocument();
  });
});
