import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { OnboardingTour } from "./onboarding-tour";

const testUser = { id: "user-1", email: "user@example.com", displayName: "Test User", emailVerified: true };

vi.mock("@/lib/auth/auth-context", () => ({
  useAuth: () => ({ user: testUser }),
}));

describe("OnboardingTour", () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it("shows the dialog when the user has not dismissed it yet", async () => {
    render(<OnboardingTour />);

    expect(await screen.findByRole("dialog")).toBeInTheDocument();
  });

  it("does not show the dialog when already dismissed for this user", () => {
    window.localStorage.setItem("sns_buzz_onboarding_dismissed_user-1", "1");
    render(<OnboardingTour />);

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("moves focus into the dialog when it opens", async () => {
    render(<OnboardingTour />);
    const dialog = await screen.findByRole("dialog");

    await waitFor(() => expect(dialog.contains(document.activeElement)).toBe(true));
  });

  it("closes and records dismissal when the primary button is clicked", async () => {
    render(<OnboardingTour />);
    await screen.findByRole("dialog");

    fireEvent.click(screen.getByRole("button", { name: "はじめる" }));

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    expect(window.localStorage.getItem("sns_buzz_onboarding_dismissed_user-1")).toBe("1");
  });

  it("closes and records dismissal when Escape is pressed", async () => {
    render(<OnboardingTour />);
    const dialog = await screen.findByRole("dialog");

    fireEvent.keyDown(dialog, { key: "Escape" });

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    expect(window.localStorage.getItem("sns_buzz_onboarding_dismissed_user-1")).toBe("1");
  });

  it("traps Tab focus: pressing Tab on the last focusable element wraps to the first", async () => {
    render(<OnboardingTour />);
    const dialog = await screen.findByRole("dialog");
    const primaryButton = screen.getByRole("button", { name: "はじめる" });
    const firstLink = screen.getByRole("link", { name: /投稿分析/ });

    primaryButton.focus();
    expect(document.activeElement).toBe(primaryButton);

    fireEvent.keyDown(dialog, { key: "Tab" });

    expect(document.activeElement).toBe(firstLink);
  });

  it("traps Shift+Tab focus: pressing Shift+Tab on the first focusable element wraps to the last", async () => {
    render(<OnboardingTour />);
    const dialog = await screen.findByRole("dialog");
    const primaryButton = screen.getByRole("button", { name: "はじめる" });
    const firstLink = screen.getByRole("link", { name: /投稿分析/ });

    firstLink.focus();
    expect(document.activeElement).toBe(firstLink);

    fireEvent.keyDown(dialog, { key: "Tab", shiftKey: true });

    expect(document.activeElement).toBe(primaryButton);
  });
});
