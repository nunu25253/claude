import { afterEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { RegisterForm } from "./register-form";
import { ApiError } from "@/lib/types/common";

const registerMock = vi.fn();
const pushMock = vi.fn();

vi.mock("@/lib/auth/auth-context", () => ({
  useAuth: () => ({ register: registerMock }),
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: pushMock }),
}));

// TurnstileWidgetはNEXT_PUBLIC_TURNSTILE_SITE_KEY未設定時は何も描画しない実コンポーネントを
// そのまま使う(このテストでは未設定のため、captchaTokenは常にundefinedのまま送信される)。

function fillAndSubmit(values: {
  displayName?: string;
  email?: string;
  password?: string;
  passwordConfirm?: string;
  acceptTerms?: boolean;
}) {
  if (values.displayName !== undefined) {
    fireEvent.change(screen.getByLabelText("表示名"), { target: { value: values.displayName } });
  }
  if (values.email !== undefined) {
    fireEvent.change(screen.getByLabelText("メールアドレス"), { target: { value: values.email } });
  }
  if (values.password !== undefined) {
    fireEvent.change(screen.getByLabelText("パスワード"), { target: { value: values.password } });
  }
  if (values.passwordConfirm !== undefined) {
    fireEvent.change(screen.getByLabelText("パスワード（確認）"), {
      target: { value: values.passwordConfirm },
    });
  }
  if (values.acceptTerms ?? true) {
    fireEvent.click(screen.getByRole("checkbox"));
  }
  fireEvent.click(screen.getByRole("button", { name: "新規登録" }));
}

describe("RegisterForm", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("shows validation errors when submitting an empty form", async () => {
    render(<RegisterForm />);

    fireEvent.click(screen.getByRole("button", { name: "新規登録" }));

    expect(await screen.findByText("表示名を入力してください")).toBeInTheDocument();
    expect(screen.getByText("メールアドレスを入力してください")).toBeInTheDocument();
    expect(screen.getByText("パスワードは8文字以上で入力してください")).toBeInTheDocument();
    expect(screen.getByText("確認用パスワードを入力してください")).toBeInTheDocument();
    expect(screen.getByText("利用規約とプライバシーポリシーへの同意が必要です")).toBeInTheDocument();
    expect(registerMock).not.toHaveBeenCalled();
  });

  it("blocks submission when the terms checkbox is left unchecked", async () => {
    render(<RegisterForm />);

    fillAndSubmit({
      displayName: "テストユーザー",
      email: "user@example.com",
      password: "password123",
      passwordConfirm: "password123",
      acceptTerms: false,
    });

    expect(await screen.findByText("利用規約とプライバシーポリシーへの同意が必要です")).toBeInTheDocument();
    expect(registerMock).not.toHaveBeenCalled();
  });

  it("shows a mismatch error when password and confirmation differ", async () => {
    render(<RegisterForm />);

    fillAndSubmit({
      displayName: "テストユーザー",
      email: "user@example.com",
      password: "password123",
      passwordConfirm: "different123",
    });

    expect(await screen.findByText("パスワードが一致しません")).toBeInTheDocument();
    expect(registerMock).not.toHaveBeenCalled();
  });

  it("registers the user and redirects to '/' on success", async () => {
    registerMock.mockResolvedValue(undefined);
    render(<RegisterForm />);

    fillAndSubmit({
      displayName: "テストユーザー",
      email: "user@example.com",
      password: "password123",
      passwordConfirm: "password123",
    });

    await waitFor(() =>
      expect(registerMock).toHaveBeenCalledWith({
        displayName: "テストユーザー",
        email: "user@example.com",
        password: "password123",
        captchaToken: undefined,
      }),
    );
    await waitFor(() => expect(pushMock).toHaveBeenCalledWith("/"));
  });

  it("shows the ApiError message when registration fails", async () => {
    registerMock.mockRejectedValue(new ApiError(400, { message: "このメールアドレスは既に登録されています" }));
    render(<RegisterForm />);

    fillAndSubmit({
      displayName: "テストユーザー",
      email: "existing@example.com",
      password: "password123",
      passwordConfirm: "password123",
    });

    expect(await screen.findByRole("alert")).toHaveTextContent("このメールアドレスは既に登録されています");
    expect(pushMock).not.toHaveBeenCalled();
  });

  it("shows a generic error message when registration fails with a non-ApiError", async () => {
    registerMock.mockRejectedValue(new Error("network down"));
    render(<RegisterForm />);

    fillAndSubmit({
      displayName: "テストユーザー",
      email: "user@example.com",
      password: "password123",
      passwordConfirm: "password123",
    });

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "登録に失敗しました。時間をおいて再度お試しください。",
    );
  });
});
