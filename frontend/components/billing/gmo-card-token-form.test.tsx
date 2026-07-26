import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";

const SHOP_ID = "shop-123";
const SCRIPT_SRC = "https://static.mul-pay.jp/ext/js/token.js";

describe("GmoCardTokenForm", () => {
  const originalShopId = process.env.NEXT_PUBLIC_GMO_SHOP_ID;

  beforeEach(() => {
    vi.resetModules();
    delete window.Multipayment;
    document.querySelectorAll(`script[src="${SCRIPT_SRC}"]`).forEach((el) => el.remove());
  });

  afterEach(() => {
    cleanup();
    if (originalShopId === undefined) {
      delete process.env.NEXT_PUBLIC_GMO_SHOP_ID;
    } else {
      process.env.NEXT_PUBLIC_GMO_SHOP_ID = originalShopId;
    }
  });

  it("falls back to a manual token input when NEXT_PUBLIC_GMO_SHOP_ID is not set", async () => {
    delete process.env.NEXT_PUBLIC_GMO_SHOP_ID;
    const { GmoCardTokenForm } = await import("./gmo-card-token-form");
    const onToken = vi.fn();

    render(<GmoCardTokenForm onToken={onToken} isSubmitting={false} />);

    fireEvent.change(screen.getByPlaceholderText("tok_xxxxxxxx"), { target: { value: "tok_manual" } });
    fireEvent.click(screen.getByRole("button", { name: "アップグレードする" }));

    expect(onToken).toHaveBeenCalledWith("tok_manual");
    expect(document.head.querySelector(`script[src="${SCRIPT_SRC}"]`)).toBeNull();
  });

  it("loads the GMO tokenization script and initializes Multipayment when a shop id is set", async () => {
    process.env.NEXT_PUBLIC_GMO_SHOP_ID = SHOP_ID;
    const initMock = vi.fn();
    const { GmoCardTokenForm } = await import("./gmo-card-token-form");

    render(<GmoCardTokenForm onToken={vi.fn()} isSubmitting={false} />);

    const script = document.head.querySelector<HTMLScriptElement>(`script[src="${SCRIPT_SRC}"]`);
    expect(script).not.toBeNull();

    window.Multipayment = { init: initMock, getToken: vi.fn() };
    script!.onload?.(new Event("load"));

    await waitFor(() => expect(initMock).toHaveBeenCalledWith(SHOP_ID));
    expect(screen.getByLabelText("カード番号")).toBeInTheDocument();
  });

  it("calls onToken with the tokenized value on successful getToken", async () => {
    process.env.NEXT_PUBLIC_GMO_SHOP_ID = SHOP_ID;
    const getTokenMock = vi.fn((_cardInfo, callback) => {
      callback({ resultCode: "000", tokenObject: { token: ["tok_real_123"] } });
    });
    window.Multipayment = { init: vi.fn(), getToken: getTokenMock };
    const { GmoCardTokenForm } = await import("./gmo-card-token-form");
    const onToken = vi.fn();

    render(<GmoCardTokenForm onToken={onToken} isSubmitting={false} />);
    await waitFor(() => expect(window.Multipayment!.init).toHaveBeenCalledWith(SHOP_ID));

    fireEvent.change(screen.getByLabelText("カード番号"), { target: { value: "4242424242424242" } });
    fireEvent.change(screen.getByLabelText("有効期限(月)"), { target: { value: "12" } });
    fireEvent.change(screen.getByLabelText("有効期限(年)"), { target: { value: "30" } });
    fireEvent.change(screen.getByLabelText("セキュリティコード"), { target: { value: "123" } });
    fireEvent.click(screen.getByRole("button", { name: "アップグレードする" }));

    await waitFor(() => expect(onToken).toHaveBeenCalledWith("tok_real_123"));
  });

  it("shows an error and does not call onToken when tokenization fails", async () => {
    process.env.NEXT_PUBLIC_GMO_SHOP_ID = SHOP_ID;
    const getTokenMock = vi.fn((_cardInfo, callback) => {
      callback({ resultCode: "101" });
    });
    window.Multipayment = { init: vi.fn(), getToken: getTokenMock };
    const { GmoCardTokenForm } = await import("./gmo-card-token-form");
    const onToken = vi.fn();

    render(<GmoCardTokenForm onToken={onToken} isSubmitting={false} />);
    await waitFor(() => expect(window.Multipayment!.init).toHaveBeenCalledWith(SHOP_ID));

    fireEvent.change(screen.getByLabelText("カード番号"), { target: { value: "4000000000000002" } });
    fireEvent.change(screen.getByLabelText("有効期限(月)"), { target: { value: "01" } });
    fireEvent.change(screen.getByLabelText("有効期限(年)"), { target: { value: "25" } });
    fireEvent.change(screen.getByLabelText("セキュリティコード"), { target: { value: "999" } });
    fireEvent.click(screen.getByRole("button", { name: "アップグレードする" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("トークン化に失敗しました");
    expect(onToken).not.toHaveBeenCalled();
  });
});
