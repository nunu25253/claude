import { afterEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { DeleteAccountSection } from "./delete-account-section";
import { ApiError } from "@/lib/types/common";

const deleteAccountMock = vi.fn();

vi.mock("@/lib/auth/auth-context", () => ({
  useAuth: () => ({ deleteAccount: deleteAccountMock }),
}));

describe("DeleteAccountSection", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("shows only the initial warning and delete button by default", () => {
    render(<DeleteAccountSection />);

    expect(screen.getByRole("button", { name: "アカウントを削除する" })).toBeInTheDocument();
    expect(screen.queryByLabelText("現在のパスワード")).not.toBeInTheDocument();
  });

  it("reveals the password confirmation form when the delete button is clicked", () => {
    render(<DeleteAccountSection />);

    fireEvent.click(screen.getByRole("button", { name: "アカウントを削除する" }));

    expect(screen.getByLabelText("現在のパスワード")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "完全に削除する" })).toBeInTheDocument();
  });

  it("cancel returns to the initial warning state", () => {
    render(<DeleteAccountSection />);

    fireEvent.click(screen.getByRole("button", { name: "アカウントを削除する" }));
    fireEvent.click(screen.getByRole("button", { name: "キャンセル" }));

    expect(screen.getByRole("button", { name: "アカウントを削除する" })).toBeInTheDocument();
    expect(screen.queryByLabelText("現在のパスワード")).not.toBeInTheDocument();
  });

  it("shows a validation error when confirming without entering a password", async () => {
    render(<DeleteAccountSection />);

    fireEvent.click(screen.getByRole("button", { name: "アカウントを削除する" }));
    fireEvent.click(screen.getByRole("button", { name: "完全に削除する" }));

    expect(await screen.findByText("現在のパスワードを入力してください")).toBeInTheDocument();
    expect(deleteAccountMock).not.toHaveBeenCalled();
  });

  it("calls deleteAccount with the entered password on confirm", async () => {
    deleteAccountMock.mockResolvedValue(undefined);
    render(<DeleteAccountSection />);

    fireEvent.click(screen.getByRole("button", { name: "アカウントを削除する" }));
    fireEvent.change(screen.getByLabelText("現在のパスワード"), {
      target: { value: "Password123!" },
    });
    fireEvent.click(screen.getByRole("button", { name: "完全に削除する" }));

    await waitFor(() => expect(deleteAccountMock).toHaveBeenCalledWith("Password123!"));
  });

  it("shows the ApiError message when deletion fails due to an incorrect password", async () => {
    deleteAccountMock.mockRejectedValue(
      new ApiError(400, { code: "DELETE_ACCOUNT_INVALID_PASSWORD", message: "パスワードが正しくありません。" }),
    );
    render(<DeleteAccountSection />);

    fireEvent.click(screen.getByRole("button", { name: "アカウントを削除する" }));
    fireEvent.change(screen.getByLabelText("現在のパスワード"), {
      target: { value: "wrong-password" },
    });
    fireEvent.click(screen.getByRole("button", { name: "完全に削除する" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("パスワードが正しくありません。");
  });

  it("shows a generic error message when deletion fails with a non-ApiError", async () => {
    deleteAccountMock.mockRejectedValue(new Error("network down"));
    render(<DeleteAccountSection />);

    fireEvent.click(screen.getByRole("button", { name: "アカウントを削除する" }));
    fireEvent.change(screen.getByLabelText("現在のパスワード"), {
      target: { value: "Password123!" },
    });
    fireEvent.click(screen.getByRole("button", { name: "完全に削除する" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "アカウントの削除に失敗しました。時間をおいて再度お試しください。",
    );
  });
});
