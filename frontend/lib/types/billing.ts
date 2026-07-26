import type { components } from "./generated/api";

// バックエンドの実装(springdoc生成OpenAPIスキーマ)を一次ソースとする(#58と同じ方針)。
// currentPeriodEndはFREEプランではnullになりうるため、Requiredでラップせず手書きで型付けする
// (springdocはJavaレコードの全フィールドをnullable指定なしでoptional扱いするため、実際の
// null/非null区別はレスポンスDTOの実装を見て手動で反映する必要がある)。
type Schemas = components["schemas"];

export type SubscriptionPlan = "FREE" | "PRO";
export type SubscriptionStatus = "ACTIVE" | "CANCELED";

export interface Subscription {
  plan: SubscriptionPlan;
  status: SubscriptionStatus;
  currentPeriodEnd: string | null;
  dailyAnalysisLimit: number;
}

export type UpgradeSubscriptionRequest = Schemas["UpgradeSubscriptionRequest"];

export interface PlanInfo {
  planId: string;
  monthlyAmountYen: number;
  dailyAnalysisLimit: number;
}

export interface BillingPlans {
  free: PlanInfo;
  pro: PlanInfo;
}
