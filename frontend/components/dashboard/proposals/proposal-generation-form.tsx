"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";
import type { ProposalGenerationRequest } from "@/lib/types";

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function parsePostIds(raw: string): string[] {
  return raw
    .split(/[\s,]+/)
    .map((s) => s.trim())
    .filter(Boolean);
}

const generationSchema = z.object({
  postIds: z
    .string()
    .min(1, "投稿IDを1件以上入力してください")
    .refine((v) => parsePostIds(v).length > 0, "投稿IDを1件以上入力してください")
    .refine((v) => parsePostIds(v).every((id) => UUID_PATTERN.test(id)), "投稿IDはUUID形式で入力してください"),
  count: z.coerce.number().int().min(1).max(20).optional(),
});

type GenerationFormValues = z.infer<typeof generationSchema>;

interface ProposalGenerationFormProps {
  onSubmit: (payload: ProposalGenerationRequest) => void;
  isSubmitting: boolean;
}

export function ProposalGenerationForm({ onSubmit, isSubmitting }: ProposalGenerationFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<GenerationFormValues>({ resolver: zodResolver(generationSchema) });

  return (
    <form
      noValidate
      className="space-y-4"
      onSubmit={handleSubmit((values) => {
        onSubmit({ postIds: parsePostIds(values.postIds), count: values.count });
      })}
    >
      <FormField
        label="投稿ID（共通点分析の対象。最大100件、カンマまたは改行区切り）"
        htmlFor="postIds"
        error={errors.postIds?.message}
      >
        <textarea
          id="postIds"
          rows={4}
          placeholder="11111111-1111-1111-1111-111111111111&#10;22222222-2222-2222-2222-222222222222"
          className={inputClassName(!!errors.postIds)}
          {...register("postIds")}
        />
      </FormField>
      <div className="flex items-end gap-3">
        <FormField label="生成件数（省略時20件、上限20件）" htmlFor="count" error={errors.count?.message}>
          <input
            id="count"
            type="number"
            min={1}
            max={20}
            placeholder="20"
            className={inputClassName(!!errors.count)}
            {...register("count")}
          />
        </FormField>
        <Button type="submit" isLoading={isSubmitting} className="shrink-0">
          AIで企画を生成する
        </Button>
      </div>
    </form>
  );
}
