package com.buzzanalysis.domain.billing;

/** サブスクリプションの状態。 */
public enum SubscriptionStatus {
    /** 有効(PROプランは決済成功中、FREEプランは常にACTIVE)。 */
    ACTIVE,
    /** ユーザーがキャンセルし、FREEプランへ戻った状態。 */
    CANCELED
}
