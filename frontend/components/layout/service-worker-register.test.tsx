import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, render } from "@testing-library/react";
import { ServiceWorkerRegister } from "./service-worker-register";

describe("ServiceWorkerRegister", () => {
  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
  });

  it("registers /sw.js when the Service Worker API is available", () => {
    const register = vi.fn().mockResolvedValue(undefined);
    vi.stubGlobal("navigator", { serviceWorker: { register } });

    render(<ServiceWorkerRegister />);

    expect(register).toHaveBeenCalledWith("/sw.js");
  });

  it("does nothing when the Service Worker API is unavailable", () => {
    vi.stubGlobal("navigator", {});

    expect(() => render(<ServiceWorkerRegister />)).not.toThrow();
  });

  it("renders nothing", () => {
    vi.stubGlobal("navigator", { serviceWorker: { register: vi.fn().mockResolvedValue(undefined) } });
    const { container } = render(<ServiceWorkerRegister />);

    expect(container).toBeEmptyDOMElement();
  });
});
