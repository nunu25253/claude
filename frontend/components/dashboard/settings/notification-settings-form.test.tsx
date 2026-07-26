import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { NotificationSettingsForm } from "./notification-settings-form";
import type { NotificationSettings } from "@/lib/types";

const updateMutate = vi.fn();
let settingsData: NotificationSettings | undefined;

vi.mock("@/lib/hooks/use-settings", () => ({
  useNotificationSettings: () => ({ data: settingsData, isLoading: false, isError: false, refetch: vi.fn() }),
  useUpdateNotificationSettings: () => ({ mutate: updateMutate, isPending: false, isSuccess: false, isError: false }),
}));

describe("NotificationSettingsForm", () => {
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
    settingsData = undefined;
  });

  it("renders the Slack webhook URL field pre-filled from loaded settings", () => {
    settingsData = {
      emailOnAnalysisComplete: true,
      emailWeeklyDigest: false,
      emailTrendingAlert: false,
      slackWebhookUrl: "https://hooks.slack.com/services/existing",
    };
    render(<NotificationSettingsForm />);

    const input = screen.getByLabelText<HTMLInputElement>("Slack通知(任意)");
    expect(input.value).toBe("https://hooks.slack.com/services/existing");
  });

  it("submits the edited Slack webhook URL together with the toggle values", async () => {
    settingsData = {
      emailOnAnalysisComplete: true,
      emailWeeklyDigest: false,
      emailTrendingAlert: false,
      slackWebhookUrl: null,
    };
    render(<NotificationSettingsForm />);

    fireEvent.change(screen.getByLabelText("Slack通知(任意)"), {
      target: { value: "https://hooks.slack.com/services/new" },
    });
    fireEvent.click(screen.getByRole("button", { name: "保存する" }));

    await waitFor(() =>
      expect(updateMutate).toHaveBeenCalledWith(
        expect.objectContaining({ slackWebhookUrl: "https://hooks.slack.com/services/new" }),
      ),
    );
  });
});
