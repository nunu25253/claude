"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";
import { useCreateOrganization } from "@/lib/hooks/use-organizations";
import type { Organization } from "@/lib/types";

const schema = z.object({
  name: z.string().min(1, "チーム名を入力してください").max(200),
});

type FormValues = z.infer<typeof schema>;

export function CreateOrganizationForm({ onCreated }: { onCreated: (org: Organization) => void }) {
  const createMutation = useCreateOrganization();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { name: "" } });

  const onSubmit = async (values: FormValues) => {
    const created = await createMutation.mutateAsync(values);
    reset();
    onCreated(created);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="flex items-start gap-3">
      <div className="flex-1">
        <FormField label="チーム名" htmlFor="organizationName" error={errors.name?.message}>
          <input
            id="organizationName"
            placeholder="例: マーケティングチーム"
            className={inputClassName(!!errors.name)}
            {...register("name")}
          />
        </FormField>
      </div>
      <div className="pt-6">
        <Button type="submit" isLoading={createMutation.isPending}>
          作成する
        </Button>
      </div>
      {createMutation.isError && (
        <p className="pt-6 text-sm text-red-500">作成に失敗しました</p>
      )}
    </form>
  );
}
