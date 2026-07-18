"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";
import type { PostEvaluationRequest } from "@/lib/types";

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

const evaluationSchema = z.object({
  proposalId: z
    .string()
    .optional()
    .refine((v) => !v || UUID_PATTERN.test(v), "企画IDはUUID形式で入力してください"),
  title: z.string().min(1, "タイトルを入力してください"),
  hookText: z.string().optional(),
  structureText: z.string().optional(),
  ctaText: z.string().optional(),
  targetAudienceText: z.string().optional(),
});

type EvaluationFormValues = z.infer<typeof evaluationSchema>;

interface EvaluationFormProps {
  onSubmit: (payload: PostEvaluationRequest) => void;
  isSubmitting: boolean;
}

export function EvaluationForm({ onSubmit, isSubmitting }: EvaluationFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<EvaluationFormValues>({ resolver: zodResolver(evaluationSchema) });

  return (
    <form
      noValidate
      className="space-y-4"
      onSubmit={handleSubmit((values) =>
        onSubmit({ ...values, proposalId: values.proposalId || undefined }),
      )}
    >
      <FormField
        label="企画ID（任意。指定すると元企画との一致率を算出）"
        htmlFor="proposalId"
        error={errors.proposalId?.message}
      >
        <input
          id="proposalId"
          placeholder="11111111-1111-1111-1111-111111111111"
          className={inputClassName(!!errors.proposalId)}
          {...register("proposalId")}
        />
      </FormField>
      <FormField label="タイトル" htmlFor="title" error={errors.title?.message}>
        <input id="title" className={inputClassName(!!errors.title)} {...register("title")} />
      </FormField>
      <FormField label="フック" htmlFor="hookText" error={errors.hookText?.message}>
        <textarea id="hookText" rows={2} className={inputClassName(!!errors.hookText)} {...register("hookText")} />
      </FormField>
      <FormField label="構成" htmlFor="structureText" error={errors.structureText?.message}>
        <textarea
          id="structureText"
          rows={3}
          className={inputClassName(!!errors.structureText)}
          {...register("structureText")}
        />
      </FormField>
      <FormField label="CTA" htmlFor="ctaText" error={errors.ctaText?.message}>
        <input id="ctaText" className={inputClassName(!!errors.ctaText)} {...register("ctaText")} />
      </FormField>
      <FormField
        label="想定ターゲット"
        htmlFor="targetAudienceText"
        error={errors.targetAudienceText?.message}
      >
        <input
          id="targetAudienceText"
          className={inputClassName(!!errors.targetAudienceText)}
          {...register("targetAudienceText")}
        />
      </FormField>
      <Button type="submit" isLoading={isSubmitting}>
        AIで評価する
      </Button>
    </form>
  );
}
