package com.buzzanalysis.infrastructure.notification;

import com.buzzanalysis.domain.notification.SlackNotifierPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Slack Incoming Webhook(ユーザーがSlack側で発行しSettings画面に登録したURL)へPOSTするアダプタ。
 * webhookUrlはユーザー任意入力のため、送信失敗(URL誤り・Slack側の障害等)でアプリ内通知の作成自体を
 * 失敗させてはならない({@link com.buzzanalysis.infrastructure.mail.SmtpMailSenderAdapter}と同じ
 * ベストエフォート方針)。
 */
@Component
public class SlackWebhookNotifierAdapter implements SlackNotifierPort {

    private static final Logger log = LoggerFactory.getLogger(SlackWebhookNotifierAdapter.class);

    private final RestClient restClient;

    public SlackWebhookNotifierAdapter(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendMessage(String webhookUrl, String text) {
        try {
            restClient.post()
                    .uri(webhookUrl)
                    .body(Map.of("text", text))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Failed to deliver Slack webhook notification", e);
        }
    }
}
