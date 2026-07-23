package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/** BuzzScoreしきい値アラートの設定リクエスト。thresholdにnullを指定するとアラートを解除する。 */
public record SetAlertThresholdRequest(
        @DecimalMin("0") @DecimalMax("100") Double threshold
) {
}
