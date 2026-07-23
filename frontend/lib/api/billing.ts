import { apiClient } from "../api-client";
import type { Subscription, UpgradeSubscriptionRequest } from "../types";

export const billingApi = {
  getMyPlan: () => apiClient.get<Subscription>("/billing/subscription"),

  upgrade: (payload: UpgradeSubscriptionRequest) =>
    apiClient.post<Subscription>("/billing/subscription/upgrade", payload),

  cancel: () => apiClient.post<Subscription>("/billing/subscription/cancel"),
};
