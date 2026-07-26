import type { Metadata } from "next";
import { ForgotPasswordForm } from "@/components/auth/forgot-password-form";

export const metadata: Metadata = { title: "パスワードをお忘れの方" };

export default function ForgotPasswordPage() {
  return <ForgotPasswordForm />;
}
