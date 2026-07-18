"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";

const urlSchema = z.object({
  url: z
    .string()
    .min(1, "投稿URLを入力してください")
    .url("正しいURL形式で入力してください")
    .refine(
      (v) => /instagram\.com|tiktok\.com|x\.com|twitter\.com/i.test(v),
      "Instagram / TikTok / X の投稿URLを入力してください",
    ),
});

type UrlFormValues = z.infer<typeof urlSchema>;

interface AnalyzeUrlFormProps {
  onSubmit: (url: string) => void;
  isSubmitting: boolean;
}

export function AnalyzeUrlForm({ onSubmit, isSubmitting }: AnalyzeUrlFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UrlFormValues>({ resolver: zodResolver(urlSchema) });

  return (
    <form
      onSubmit={handleSubmit((values) => onSubmit(values.url))}
      noValidate
      className="flex flex-col gap-3 sm:flex-row sm:items-start"
    >
      <div className="flex-1">
        <FormField label="投稿URL" htmlFor="url" error={errors.url?.message}>
          <input
            id="url"
            type="url"
            placeholder="https://www.instagram.com/p/xxxxxxxxx/"
            className={inputClassName(!!errors.url)}
            {...register("url")}
          />
        </FormField>
      </div>
      <Button type="submit" isLoading={isSubmitting} className="mt-0 shrink-0 sm:mt-6">
        AIで分析する
      </Button>
    </form>
  );
}
