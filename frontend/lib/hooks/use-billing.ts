import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { billingApi } from "@/lib/api";
import type { UpgradeSubscriptionRequest } from "@/lib/types";

const SUBSCRIPTION_KEY = ["billing", "subscription"];
const PLANS_KEY = ["billing", "plans"];

export function useSubscription() {
  return useQuery({
    queryKey: SUBSCRIPTION_KEY,
    queryFn: () => billingApi.getMyPlan(),
  });
}

// 料金・利用上限はバックエンドの設定値(UsageQuotaProperties/GmoPaymentProperties)を
// 唯一の情報源とする(シニアレビュー: 手動同期前提だった旧PLAN_COMPARISON定数の解消)。
export function useBillingPlans() {
  return useQuery({
    queryKey: PLANS_KEY,
    queryFn: () => billingApi.getPlans(),
    staleTime: 5 * 60 * 1000,
  });
}

export function useUpgradeSubscription() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: UpgradeSubscriptionRequest) => billingApi.upgrade(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: SUBSCRIPTION_KEY });
    },
  });
}

export function useCancelSubscription() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => billingApi.cancel(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: SUBSCRIPTION_KEY });
    },
  });
}
