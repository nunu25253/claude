package com.buzzanalysis.infrastructure.payment;

import com.buzzanalysis.domain.billing.PaymentChargeResult;
import com.buzzanalysis.domain.billing.PaymentGatewayPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * {@link PaymentGatewayPort} のGMOペイメントゲートウェイ(GMO-PG)マルチペイメントサービス実装。
 * {@code payment.gateway.provider=gmo} を指定した場合のみ有効になる（未指定時は{@link MockPaymentGatewayAdapter}）。
 *
 * <p><b>重要: 本番投入前に必ず確認すること。</b>
 * エンドポイントパス・パラメータ名はGMO-PGの一般的なマルチペイメントサービスAPI(会員登録/カード登録/
 * 取引登録・実行の form-urlencoded 方式)の形状に基づく実装であり、契約時にGMOから提供される
 * 技術仕様書と突き合わせた検証を経ていない。ShopID/ShopPassが未契約のため、このアダプタ自体も
 * 実APIに対する疎通確認は行えていない(デフォルトでは{@link MockPaymentGatewayAdapter}が使われ、
 * このクラスは {@code payment.gateway.provider=gmo} を明示した場合のみ有効化される)。</p>
 *
 * <p>GMO-PGの基本APIには「継続課金」を単一で開始するエンドポイントは無く、初回に会員・カードを
 * 登録した上で毎月分を都度課金する運用となる。本アダプタは初回課金までを担い、月次の再課金は
 * {@link com.buzzanalysis.infrastructure.scheduling.BillingRenewalScheduler} が
 * {@link #chargeRenewal} を定期実行することで実現する(マーチャント側で再課金タイミングを管理する、
 * GMO-PGでの一般的な継続課金の実装方式)。</p>
 */
@Component
@ConditionalOnProperty(prefix = "payment.gateway", name = "provider", havingValue = "gmo")
public class GmoPaymentGatewayAdapter implements PaymentGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(GmoPaymentGatewayAdapter.class);

    private static final String SAVE_MEMBER_PATH = "/payment/SaveMember.idPass";
    private static final String DELETE_MEMBER_PATH = "/payment/DeleteMember.idPass";
    private static final String SAVE_CARD_PATH = "/payment/SaveCard.idPass";
    private static final String ENTRY_TRAN_PATH = "/payment/EntryTran.idPass";
    private static final String EXEC_TRAN_PATH = "/payment/ExecTran.idPass";

    private final GmoPaymentProperties properties;
    private final WebClient webClient;

    public GmoPaymentGatewayAdapter(GmoPaymentProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.getBaseUrl()).build();
        log.warn("GMO payment gateway adapter is active. Endpoint shapes are unverified against a real GMO-PG "
                + "contract (see class Javadoc) — confirm against the official technical specification before "
                + "processing real charges.");
    }

    @Override
    public PaymentChargeResult registerMemberAndStartRecurringCharge(UUID userId, String email, String cardToken) {
        String memberId = userId.toString();
        try {
            callGmoApi(SAVE_MEMBER_PATH, Map.of(
                    "MemberID", memberId,
                    "MemberName", email
            ));

            Map<String, String> cardResponse = callGmoApi(SAVE_CARD_PATH, Map.of(
                    "MemberID", memberId,
                    "Token", cardToken
            ));
            String cardSeq = cardResponse.getOrDefault("CardSeq", "0");

            Map<String, String> entryResponse = callGmoApi(ENTRY_TRAN_PATH, Map.of(
                    "OrderID", "sub-" + memberId + "-" + System.currentTimeMillis(),
                    "Amount", String.valueOf(properties.getProPlanMonthlyAmount())
            ));
            callGmoApi(EXEC_TRAN_PATH, Map.of(
                    "AccessID", entryResponse.getOrDefault("AccessID", ""),
                    "AccessPass", entryResponse.getOrDefault("AccessPass", ""),
                    "MemberID", memberId,
                    "CardSeq", cardSeq
            ));

            return PaymentChargeResult.success(memberId);
        } catch (Exception e) {
            log.error("GMO payment registration failed for userId={}", userId, e);
            return PaymentChargeResult.failure("決済処理に失敗しました。カード情報をご確認の上、再度お試しください。");
        }
    }

    @Override
    public void cancelRecurringCharge(String paymentProviderMemberId) {
        try {
            callGmoApi(DELETE_MEMBER_PATH, Map.of("MemberID", paymentProviderMemberId));
        } catch (Exception e) {
            log.error("GMO member deletion failed for paymentProviderMemberId={}", paymentProviderMemberId, e);
        }
    }

    @Override
    public PaymentChargeResult chargeRenewal(String paymentProviderMemberId) {
        try {
            Map<String, String> entryResponse = callGmoApi(ENTRY_TRAN_PATH, Map.of(
                    "OrderID", "renewal-" + paymentProviderMemberId + "-" + System.currentTimeMillis(),
                    "Amount", String.valueOf(properties.getProPlanMonthlyAmount())
            ));
            callGmoApi(EXEC_TRAN_PATH, Map.of(
                    "AccessID", entryResponse.getOrDefault("AccessID", ""),
                    "AccessPass", entryResponse.getOrDefault("AccessPass", ""),
                    "MemberID", paymentProviderMemberId
            ));
            return PaymentChargeResult.success(paymentProviderMemberId);
        } catch (Exception e) {
            log.error("GMO renewal charge failed for paymentProviderMemberId={}", paymentProviderMemberId, e);
            return PaymentChargeResult.failure("更新の課金に失敗しました。");
        }
    }

    /**
     * GMO-PG APIを form-urlencoded POSTで呼び出す。ShopID/ShopPassを共通パラメータとして付与し、
     * 応答本文(GMO-PGの応答はJSONではなく {@code key1=val1&key2=val2} 形式)をMapへ変換する。
     * {@code ErrCode} が含まれる場合はエラー応答とみなし例外を投げる。
     */
    private Map<String, String> callGmoApi(String path, Map<String, String> params) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("ShopID", properties.getShopId());
        form.add("ShopPass", properties.getShopPass());
        params.forEach(form::add);

        String responseBody = webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Map<String, String> response = parseFormEncodedResponse(responseBody);
        if (response.containsKey("ErrCode")) {
            throw new IllegalStateException("GMO API error: ErrCode=" + response.get("ErrCode")
                    + ", ErrInfo=" + response.get("ErrInfo"));
        }
        return response;
    }

    private Map<String, String> parseFormEncodedResponse(String body) {
        Map<String, String> result = new LinkedHashMap<>();
        if (body == null || body.isBlank()) {
            return result;
        }
        for (String pair : body.split("&")) {
            int idx = pair.indexOf('=');
            if (idx < 0) continue;
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            result.put(key, value);
        }
        return result;
    }
}
