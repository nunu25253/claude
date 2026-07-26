import { describe, expect, it } from "vitest";
import { scorePasswordStrength } from "./password-strength";

describe("scorePasswordStrength", () => {
  it("scores an empty password as the weakest", () => {
    expect(scorePasswordStrength("")).toEqual({ score: 0, label: "とても弱い" });
  });

  it("scores a common weak password as the weakest regardless of length", () => {
    expect(scorePasswordStrength("password123").score).toBe(0);
  });

  it("scores a short single-character-class password low", () => {
    expect(scorePasswordStrength("aaaaaaaa").score).toBeLessThanOrEqual(1);
  });

  it("scores a long password mixing character classes highly", () => {
    expect(scorePasswordStrength("Tr0ub4dor&3xample!").score).toBe(4);
  });

  it("never returns a score outside 0-4", () => {
    const { score } = scorePasswordStrength("A".repeat(50) + "1!aB2@");
    expect(score).toBeGreaterThanOrEqual(0);
    expect(score).toBeLessThanOrEqual(4);
  });
});
