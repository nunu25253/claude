package com.buzzanalysis.domain.billing;

/** 課金プラン(改善計画No.11: 無料枠/有料枠)。具体的な利用上限値はinfrastructure層の設定値で管理する。 */
public enum SubscriptionPlan {
    FREE,
    PRO
}
