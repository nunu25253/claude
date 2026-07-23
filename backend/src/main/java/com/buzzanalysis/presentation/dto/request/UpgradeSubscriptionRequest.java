package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * PROプランへのアップグレードリクエスト。
 * {@code cardToken} は決済代行事業者のクライアントサイドJS(GMOの場合TokenObject.js)でブラウザ上
 * トークン化されたカード情報の参照値であり、生のカード番号は受け取らない(PCI DSSスコープの極小化)。
 */
public record UpgradeSubscriptionRequest(
        @NotBlank String cardToken
) {
}
