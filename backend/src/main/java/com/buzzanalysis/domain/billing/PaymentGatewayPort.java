package com.buzzanalysis.domain.billing;

import java.util.UUID;

/**
 * 決済代行事業者(GMOペイメント等)との連携を抽象化するポート（Strategy/Adapter）。
 * 実装はinfrastructure層に置き、{@code payment.gateway.provider} 設定で切り替える
 * (未契約環境向けの既定実装はモック。本番投入時は {@code gmo} を指定する)。
 *
 * <p>カード番号(PAN)そのものは本ポートの引数として受け取らない。決済代行事業者が提供する
 * クライアントサイドの トークン化JS(GMOの場合 TokenObject.js)でブラウザ上でトークン化した
 * 参照値のみを受け渡す設計とし、生のカード番号がこのバックエンドを経由しない(PCI DSSスコープの
 * 極小化)ようにしている。</p>
 */
public interface PaymentGatewayPort {

    /**
     * 決済代行事業者側に会員を登録し、渡されたカードトークンで継続課金(月額)を開始する。
     *
     * @param userId    課金対象のユーザーID
     * @param email     決済代行事業者側の会員登録に使うメールアドレス
     * @param cardToken クライアントサイドでトークン化されたカード情報の参照値
     * @return 成功可否と、成功時は決済代行事業者側の会員IDを含む結果
     */
    PaymentChargeResult registerMemberAndStartRecurringCharge(UUID userId, String email, String cardToken);

    /**
     * 継続課金を解約する。
     *
     * @param paymentProviderMemberId {@link #registerMemberAndStartRecurringCharge} で払い出された会員ID
     */
    void cancelRecurringCharge(String paymentProviderMemberId);

    /**
     * 登録済みの会員・カードに対して次月分の課金を実行する({@link com.buzzanalysis.infrastructure.scheduling}の
     * 更新バッチから、契約更新日を迎えたPRO契約に対して呼び出される)。
     *
     * @param paymentProviderMemberId {@link #registerMemberAndStartRecurringCharge} で払い出された会員ID
     */
    PaymentChargeResult chargeRenewal(String paymentProviderMemberId);
}
