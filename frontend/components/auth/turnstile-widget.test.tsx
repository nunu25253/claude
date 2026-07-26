import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, waitFor } from "@testing-library/react";

const SITE_KEY = "test-site-key";
const SCRIPT_SRC = "https://challenges.cloudflare.com/turnstile/v0/api.js";

describe("TurnstileWidget", () => {
  const originalSiteKey = process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY;

  beforeEach(() => {
    vi.resetModules();
    delete window.turnstile;
    document.querySelectorAll(`script[src="${SCRIPT_SRC}"]`).forEach((el) => el.remove());
  });

  afterEach(() => {
    cleanup();
    if (originalSiteKey === undefined) {
      delete process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY;
    } else {
      process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY = originalSiteKey;
    }
  });

  it("renders nothing when NEXT_PUBLIC_TURNSTILE_SITE_KEY is not set", async () => {
    delete process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY;
    const { TurnstileWidget } = await import("./turnstile-widget");

    const { container } = render(<TurnstileWidget onVerify={vi.fn()} />);

    expect(container).toBeEmptyDOMElement();
  });

  it("appends the Turnstile script to document.head and renders the widget once it loads", async () => {
    process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY = SITE_KEY;
    const renderMock = vi.fn().mockReturnValue("widget-id-1");
    const { TurnstileWidget } = await import("./turnstile-widget");

    render(<TurnstileWidget onVerify={vi.fn()} />);

    const script = document.head.querySelector<HTMLScriptElement>(`script[src="${SCRIPT_SRC}"]`);
    expect(script).not.toBeNull();

    // 実ブラウザでのスクリプト読み込み完了・Turnstile側によるグローバルAPI登録を模擬する。
    window.turnstile = { render: renderMock, remove: vi.fn() };
    script!.onload?.(new Event("load"));

    await waitFor(() => expect(renderMock).toHaveBeenCalledTimes(1));
    expect(renderMock.mock.calls[0]?.[1]).toMatchObject({ sitekey: SITE_KEY, theme: "auto" });
  });

  it("does not append a second script tag when window.turnstile is already loaded", async () => {
    process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY = SITE_KEY;
    window.turnstile = { render: vi.fn().mockReturnValue("widget-id-2"), remove: vi.fn() };
    const { TurnstileWidget } = await import("./turnstile-widget");

    render(<TurnstileWidget onVerify={vi.fn()} />);
    await waitFor(() => expect(window.turnstile!.render).toHaveBeenCalledTimes(1));

    expect(document.head.querySelector(`script[src="${SCRIPT_SRC}"]`)).toBeNull();
  });

  it("calls onVerify with the token produced by the Turnstile callback", async () => {
    process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY = SITE_KEY;
    let capturedCallback: ((token: string) => void) | undefined;
    window.turnstile = {
      render: vi.fn((_container, options) => {
        capturedCallback = options.callback;
        return "widget-id-3";
      }),
      remove: vi.fn(),
    };
    const { TurnstileWidget } = await import("./turnstile-widget");
    const onVerify = vi.fn();

    render(<TurnstileWidget onVerify={onVerify} />);
    await waitFor(() => expect(capturedCallback).toBeDefined());
    capturedCallback!("captcha-token-abc");

    expect(onVerify).toHaveBeenCalledWith("captcha-token-abc");
  });

  it("calls onExpire when the Turnstile expired-callback fires", async () => {
    process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY = SITE_KEY;
    let capturedExpiredCallback: (() => void) | undefined;
    window.turnstile = {
      render: vi.fn((_container, options) => {
        capturedExpiredCallback = options["expired-callback"];
        return "widget-id-4";
      }),
      remove: vi.fn(),
    };
    const { TurnstileWidget } = await import("./turnstile-widget");
    const onExpire = vi.fn();

    render(<TurnstileWidget onVerify={vi.fn()} onExpire={onExpire} />);
    await waitFor(() => expect(capturedExpiredCallback).toBeDefined());
    capturedExpiredCallback!();

    expect(onExpire).toHaveBeenCalledTimes(1);
  });

  it("removes the widget on unmount", async () => {
    process.env.NEXT_PUBLIC_TURNSTILE_SITE_KEY = SITE_KEY;
    const removeMock = vi.fn();
    window.turnstile = { render: vi.fn().mockReturnValue("widget-id-5"), remove: removeMock };
    const { TurnstileWidget } = await import("./turnstile-widget");

    const { unmount } = render(<TurnstileWidget onVerify={vi.fn()} />);
    await waitFor(() => expect(window.turnstile!.render).toHaveBeenCalledTimes(1));

    unmount();

    expect(removeMock).toHaveBeenCalledWith("widget-id-5");
  });
});
