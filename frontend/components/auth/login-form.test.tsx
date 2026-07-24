import { afterEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { LoginForm } from "./login-form";
import { ApiError } from "@/lib/types/common";

const loginMock = vi.fn();
const pushMock = vi.fn();
let searchParams = new URLSearchParams();

vi.mock("@/lib/auth/auth-context", () => ({
  useAuth: () => ({ login: loginMock }),
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: pushMock }),
  useSearchParams: () => searchParams,
}));

function fillAndSubmit(email: string, password: string) {
  fireEvent.change(screen.getByLabelText("メールアドレス"), { target: { value: email } });
  fireEvent.change(screen.getByLabelText("パスワード"), { target: { value: password } });
  fireEvent.click(screen.getByRole("button", { name: "ログイン" }));
}

describe("LoginForm", () => {
  afterEach(() => {
    vi.clearAllMocks();
    searchParams = new URLSearchParams();
  });

  it("shows validation errors when submitting an empty form", async () => {
    render(<LoginForm />);

    fireEvent.click(screen.getByRole("button", { name: "ログイン" }));

    expect(await screen.findByText("メールアドレスを入力してください")).toBeInTheDocument();
    expect(screen.getByText("パスワードを入力してください")).toBeInTheDocument();
    expect(loginMock).not.toHaveBeenCalled();
  });

  it("shows a format error for an invalid email", async () => {
    render(<LoginForm />);

    fillAndSubmit("not-an-email", "password123");

    expect(await screen.findByText("メールアドレスの形式が正しくありません")).toBeInTheDocument();
    expect(loginMock).not.toHaveBeenCalled();
  });

  it("logs in and redirects to '/' when there is no next param", async () => {
    loginMock.mockResolvedValue(undefined);
    render(<LoginForm />);

    fillAndSubmit("user@example.com", "password123");

    await waitFor(() =>
      expect(loginMock).toHaveBeenCalledWith({ email: "user@example.com", password: "password123" }),
    );
    await waitFor(() => expect(pushMock).toHaveBeenCalledWith("/"));
  });

  it("redirects to the next param after a successful login", async () => {
    searchParams = new URLSearchParams("next=/settings");
    loginMock.mockResolvedValue(undefined);
    render(<LoginForm />);

    fillAndSubmit("user@example.com", "password123");

    await waitFor(() => expect(pushMock).toHaveBeenCalledWith("/settings"));
  });

  it("shows the ApiError message when login fails", async () => {
    loginMock.mockRejectedValue(new ApiError(401, { message: "メールアドレスまたはパスワードが正しくありません" }));
    render(<LoginForm />);

    fillAndSubmit("user@example.com", "wrong-password");

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "メールアドレスまたはパスワードが正しくありません",
    );
    expect(pushMock).not.toHaveBeenCalled();
  });

  it("shows a generic error message when login fails with a non-ApiError", async () => {
    loginMock.mockRejectedValue(new Error("network down"));
    render(<LoginForm />);

    fillAndSubmit("user@example.com", "password123");

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "ログインに失敗しました。時間をおいて再度お試しください。",
    );
  });
});
