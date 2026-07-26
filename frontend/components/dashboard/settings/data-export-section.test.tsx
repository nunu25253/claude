import { afterEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { DataExportSection } from "./data-export-section";
import { ApiError } from "@/lib/types/common";
import type { AccountDataExport } from "@/lib/types";

const exportAccountDataMock = vi.fn();
const downloadJsonMock = vi.fn();

vi.mock("@/lib/api", () => ({
  authApi: {
    exportAccountData: (...args: unknown[]) => exportAccountDataMock(...args),
  },
}));

vi.mock("@/lib/download", () => ({
  downloadJson: (...args: unknown[]) => downloadJsonMock(...args),
}));

const sampleExport: AccountDataExport = {
  exportedAt: "2026-07-25T00:00:00Z",
  profile: {
    userId: "user-1",
    email: "user@example.com",
    displayName: "Test User",
    role: "USER",
    emailVerified: true,
    createdAt: "2026-01-01T00:00:00Z",
  },
  settings: null,
  savedAnalyses: [],
  organizationMemberships: [],
  subscription: null,
  reports: [],
};

describe("DataExportSection", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("shows the export button", () => {
    render(<DataExportSection />);

    expect(screen.getByRole("button", { name: "データをダウンロード" })).toBeInTheDocument();
  });

  it("fetches the export data and triggers a JSON download on click", async () => {
    exportAccountDataMock.mockResolvedValue(sampleExport);
    render(<DataExportSection />);

    fireEvent.click(screen.getByRole("button", { name: "データをダウンロード" }));

    await waitFor(() => expect(downloadJsonMock).toHaveBeenCalledTimes(1));
    expect(downloadJsonMock).toHaveBeenCalledWith(
      expect.stringMatching(/^buzzly-data-export-\d{4}-\d{2}-\d{2}\.json$/),
      sampleExport,
    );
  });

  it("shows the ApiError message when the export request fails", async () => {
    exportAccountDataMock.mockRejectedValue(new ApiError(500, { message: "サーバーエラーが発生しました。" }));
    render(<DataExportSection />);

    fireEvent.click(screen.getByRole("button", { name: "データをダウンロード" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("サーバーエラーが発生しました。");
    expect(downloadJsonMock).not.toHaveBeenCalled();
  });

  it("shows a generic error message when the export request fails with a non-ApiError", async () => {
    exportAccountDataMock.mockRejectedValue(new Error("network down"));
    render(<DataExportSection />);

    fireEvent.click(screen.getByRole("button", { name: "データをダウンロード" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "データのエクスポートに失敗しました。時間をおいて再度お試しください。",
    );
  });
});
