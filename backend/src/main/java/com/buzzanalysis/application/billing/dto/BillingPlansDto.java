package com.buzzanalysis.application.billing.dto;

/** FREE/PROプラン一覧。設定値(UsageQuotaProperties/GmoPaymentProperties)を唯一の情報源とする。 */
public record BillingPlansDto(PlanInfoDto free, PlanInfoDto pro) {
}
