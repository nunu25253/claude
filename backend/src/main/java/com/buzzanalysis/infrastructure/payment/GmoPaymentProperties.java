package com.buzzanalysis.infrastructure.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GMOペイメントゲートウェイ(GMO-PG)マルチペイメントサービス連携の設定。
 * application.ymlの {@code payment.gmo.*} にバインドされる。ShopID/ShopPassは契約時にGMOから払い出される。
 */
@ConfigurationProperties(prefix = "payment.gmo")
public class GmoPaymentProperties {

    /** テスト環境ベースURL。本番契約後は本番用ベースURLに差し替える。 */
    private String baseUrl = "https://pt01.mul-pay.jp";
    private String shopId = "";
    private String shopPass = "";
    /** PROプランの月額料金(円)。 */
    private int proPlanMonthlyAmount = 4980;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getShopPass() {
        return shopPass;
    }

    public void setShopPass(String shopPass) {
        this.shopPass = shopPass;
    }

    public int getProPlanMonthlyAmount() {
        return proPlanMonthlyAmount;
    }

    public void setProPlanMonthlyAmount(int proPlanMonthlyAmount) {
        this.proPlanMonthlyAmount = proPlanMonthlyAmount;
    }
}
