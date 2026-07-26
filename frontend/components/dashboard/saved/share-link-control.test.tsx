import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { ShareLinkControl } from "./share-link-control";

const createMutateAsync = vi.fn();
const revokeMutateAsync = vi.fn();
let createPending = false;
let revokePending = false;

vi.mock("@/lib/hooks/use-saved-analyses", () => ({
  useCreateShareLink: () => ({ mutateAsync: createMutateAsync, isPending: createPending }),
  useRevokeShareLink: () => ({ mutateAsync: revokeMutateAsync, isPending: revokePending }),
}));

Object.assign(navigator, { clipboard: { writeText: vi.fn().mockResolvedValue(undefined) } });

describe("ShareLinkControl", () => {
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
    createPending = false;
    revokePending = false;
  });

  it("shows a button to issue a share link initially", () => {
    render(<ShareLinkControl savedAnalysisId="saved-1" />);

    expect(screen.getByRole("button", { name: /共有リンクを発行/ })).toBeInTheDocument();
  });

  it("displays the share URL and copy/revoke controls after issuing a link", async () => {
    createMutateAsync.mockResolvedValue({ token: "abc-123", createdAt: "2026-07-26T00:00:00Z" });
    render(<ShareLinkControl savedAnalysisId="saved-1" />);

    fireEvent.click(screen.getByRole("button", { name: /共有リンクを発行/ }));

    await waitFor(() => expect(createMutateAsync).toHaveBeenCalledWith("saved-1"));
    const input = await screen.findByLabelText<HTMLInputElement>("共有リンク");
    expect(input.value).toContain("/shared/abc-123");
  });

  it("copies the share URL to the clipboard", async () => {
    createMutateAsync.mockResolvedValue({ token: "abc-123", createdAt: "2026-07-26T00:00:00Z" });
    render(<ShareLinkControl savedAnalysisId="saved-1" />);
    fireEvent.click(screen.getByRole("button", { name: /共有リンクを発行/ }));
    await screen.findByLabelText("共有リンク");

    fireEvent.click(screen.getByRole("button", { name: "コピー" }));

    await waitFor(() =>
      expect(navigator.clipboard.writeText).toHaveBeenCalledWith(expect.stringContaining("/shared/abc-123")),
    );
    expect(await screen.findByRole("button", { name: "コピーしました" })).toBeInTheDocument();
  });

  it("revokes the link and returns to the initial state", async () => {
    createMutateAsync.mockResolvedValue({ token: "abc-123", createdAt: "2026-07-26T00:00:00Z" });
    revokeMutateAsync.mockResolvedValue(undefined);
    render(<ShareLinkControl savedAnalysisId="saved-1" />);
    fireEvent.click(screen.getByRole("button", { name: /共有リンクを発行/ }));
    await screen.findByLabelText("共有リンク");

    fireEvent.click(screen.getByRole("button", { name: "共有を解除" }));

    await waitFor(() => expect(revokeMutateAsync).toHaveBeenCalledWith("saved-1"));
    expect(await screen.findByRole("button", { name: /共有リンクを発行/ })).toBeInTheDocument();
  });
});
