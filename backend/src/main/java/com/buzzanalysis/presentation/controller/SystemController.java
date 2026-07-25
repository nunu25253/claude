package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.infrastructure.external.platform.instagram.InstagramApiProperties;
import com.buzzanalysis.infrastructure.external.platform.tiktok.TikTokApiProperties;
import com.buzzanalysis.infrastructure.external.platform.x.XApiProperties;
import com.buzzanalysis.presentation.dto.response.DataModeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** システム全体の稼働状態に関するAPI。現時点ではSNS公式API連携の設定状況のみを提供する。 */
@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System", description = "システム稼働状態")
public class SystemController {

    private final InstagramApiProperties instagramApiProperties;
    private final TikTokApiProperties tikTokApiProperties;
    private final XApiProperties xApiProperties;

    public SystemController(InstagramApiProperties instagramApiProperties, TikTokApiProperties tikTokApiProperties,
                             XApiProperties xApiProperties) {
        this.instagramApiProperties = instagramApiProperties;
        this.tikTokApiProperties = tikTokApiProperties;
        this.xApiProperties = xApiProperties;
    }

    @Operation(summary = "SNS公式APIの連携状況取得",
            description = "各プラットフォームの公式APIキーが実際に設定されているか(=実データを取得するか、"
                    + "疑似データにフォールバックするか)を返す。フロントエンドのデモデータ表示バナーに使用する。")
    @GetMapping("/data-mode")
    public DataModeResponse getDataMode() {
        return DataModeResponse.of(instagramApiProperties.isConfigured(), tikTokApiProperties.isConfigured(),
                xApiProperties.isConfigured());
    }
}
