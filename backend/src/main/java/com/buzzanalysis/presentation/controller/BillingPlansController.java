package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.billing.SubscriptionApplicationService;
import com.buzzanalysis.application.billing.dto.BillingPlansDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FREE/PROプランの料金・利用上限を公開する未認証API。フロントエンドが料金を独自の定数で
 * 持つと表示と実際の課金額が食い違うリスクがあるため(シニアレビュー指摘)、設定値を
 * 唯一の情報源として提供する。
 */
@RestController
@RequestMapping("/api/v1/billing/plans")
@Tag(name = "Billing", description = "課金プラン(FREE/PRO)の照会・アップグレード・解約")
public class BillingPlansController {

    private final SubscriptionApplicationService subscriptionApplicationService;

    public BillingPlansController(SubscriptionApplicationService subscriptionApplicationService) {
        this.subscriptionApplicationService = subscriptionApplicationService;
    }

    @Operation(summary = "プラン一覧取得", description = "FREE/PROの料金と1日あたりの投稿分析上限を返す(未認証で取得可能)")
    @GetMapping
    public ResponseEntity<BillingPlansDto> getPlans() {
        return ResponseEntity.ok(subscriptionApplicationService.getPlans());
    }
}
