package com.buzzanalysis.infrastructure.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** {@link SlackWebhookNotifierAdapter} の単体テスト。 */
class SlackWebhookNotifierAdapterTest {

    private static final String WEBHOOK_URL = "https://hooks.slack.com/services/test";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void sendMessage_postsTextAsJsonToWebhookUrl() {
        mockServer.expect(requestTo(WEBHOOK_URL))
                .andExpect(method(POST))
                .andExpect(content().json("{\"text\":\"件名\\n本文\"}"))
                .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));
        SlackWebhookNotifierAdapter adapter = new SlackWebhookNotifierAdapter(restClientBuilder);

        adapter.sendMessage(WEBHOOK_URL, "件名\n本文");

        mockServer.verify();
    }

    @Test
    void sendMessage_doesNotThrow_whenWebhookCallFails() {
        mockServer.expect(requestTo(WEBHOOK_URL))
                .andExpect(method(POST))
                .andRespond(withServerError());
        SlackWebhookNotifierAdapter adapter = new SlackWebhookNotifierAdapter(restClientBuilder);

        adapter.sendMessage(WEBHOOK_URL, "件名\n本文");

        mockServer.verify();
    }
}
