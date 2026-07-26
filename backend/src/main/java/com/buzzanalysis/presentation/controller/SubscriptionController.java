package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.billing.SubscriptionApplicationService;
import com.buzzanalysis.application.billing.dto.SubscriptionDto;
import com.buzzanalysis.presentation.dto.request.UpgradeSubscriptionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** 課金プラン(無料枠/有料枠、改善計画No.11)API。 */
@RestController
@RequestMapping("/api/v1/billing/subscription")
@Tag(name = "Billing", description = "課金プラン(FREE/PRO)の照会・アップグレード・解約")
public class SubscriptionController {

    private final SubscriptionApplicationService subscriptionApplicationService;

    public SubscriptionController(SubscriptionApplicationService subscriptionApplicationService) {
        this.subscriptionApplicationService = subscriptionApplicationService;
    }

    @Operation(summary = "現在の課金プラン取得", description = "アップグレード未実施のユーザーはFREEプランとして返る")
    @GetMapping
    public ResponseEntity<SubscriptionDto> getMyPlan(Authentication authentication) {
        return ResponseEntity.ok(subscriptionApplicationService.getMyPlan(currentUserId(authentication)));
    }

    @Operation(summary = "PROプランへアップグレード",
            description = "カードトークン(決済代行事業者のクライアントサイドJSでトークン化された参照値)を指定して課金を開始する")
    @PostMapping("/upgrade")
    public ResponseEntity<SubscriptionDto> upgrade(Authentication authentication,
                                                    @Valid @RequestBody UpgradeSubscriptionRequest request) {
        SubscriptionDto result =
                subscriptionApplicationService.upgradeToPro(currentUserId(authentication), request.cardToken());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "PROプランの解約", description = "即時にFREEプランへ戻る(猶予期間は設けない)")
    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionDto> cancel(Authentication authentication) {
        return ResponseEntity.ok(subscriptionApplicationService.cancel(currentUserId(authentication)));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
