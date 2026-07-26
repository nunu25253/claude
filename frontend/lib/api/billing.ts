import { apiClient } from "../api-client";
import type { BillingPlans, Subscription, UpgradeSubscriptionRequest } from "../types";

export const billingApi = {
  getPlans: () => apiClient.get<BillingPlans>("/billing/plans"),

  getMyPlan: () => apiClient.get<Subscription>("/billing/subscription"),

  upgrade: (payload: UpgradeSubscriptionRequest) =>
    apiClient.post<Subscription>("/billing/subscription/upgrade", payload),

  cancel: () => apiClient.post<Subscription>("/billing/subscription/cancel"),
};
