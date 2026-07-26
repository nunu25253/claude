package com.buzzanalysis.application.savedanalysis;

import com.buzzanalysis.application.auth.MailSenderPort;
import com.buzzanalysis.application.notification.NotificationApplicationService;
import com.buzzanalysis.domain.notification.NotificationType;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 保存済み分析に設定されたBuzzScoreしきい値アラートをチェックし、超過していればメール通知するユースケース。
 * {@link com.buzzanalysis.infrastructure.scheduling.ThresholdAlertScheduler} から定期的に呼び出される。
 */
@Service
public class ThresholdAlertApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ThresholdAlertApplicationService.class);

    private final SavedAnalysisRepository savedAnalysisRepository;
    private final PostRepository postRepository;
    private final BuzzScoreRepository buzzScoreRepository;
    private final UserRepository userRepository;
    private final MailSenderPort mailSenderPort;
    private final NotificationApplicationService notificationApplicationService;

    public ThresholdAlertApplicationService(SavedAnalysisRepository savedAnalysisRepository,
                                             PostRepository postRepository,
                                             BuzzScoreRepository buzzScoreRepository,
                                             UserRepository userRepository,
                                             MailSenderPort mailSenderPort,
                                             NotificationApplicationService notificationApplicationService) {
        this.savedAnalysisRepository = savedAnalysisRepository;
        this.postRepository = postRepository;
        this.buzzScoreRepository = buzzScoreRepository;
        this.userRepository = userRepository;
        this.mailSenderPort = mailSenderPort;
        this.notificationApplicationService = notificationApplicationService;
    }

    /**
     * しきい値未通知の保存済み分析すべてについて、現在のBuzzScoreがしきい値以上かをチェックし、
     * 超過していればメール送信のうえ通知済みとしてマークする。1件ずつ独立して処理し、
     * 個別の失敗(投稿削除済み・ユーザー削除済み等)が他の件の処理を止めないようにする。
     *
     * @return 実際にアラートを送信した件数
     */
    @Transactional
    public int checkAndSendAlerts() {
        int triggeredCount = 0;
        for (SavedAnalysis saved : savedAnalysisRepository.findPendingAlerts()) {
            try {
                if (triggerIfNeeded(saved)) {
                    triggeredCount++;
                }
            } catch (Exception e) {
                log.error("Failed to process threshold alert for saved analysis {}", saved.getId(), e);
            }
        }
        return triggeredCount;
    }

    private boolean triggerIfNeeded(SavedAnalysis saved) {
        BuzzScore buzzScore = buzzScoreRepository.findByPostId(saved.getPostId()).orElse(null);
        if (buzzScore == null || !saved.shouldTriggerAlert(buzzScore.getTotalScore())) {
            return false;
        }
        Post post = postRepository.findById(saved.getPostId()).orElse(null);
        User user = userRepository.findById(saved.getUserId()).orElse(null);
        if (post == null || user == null) {
            log.warn("Skipping threshold alert for saved analysis {}: post or user no longer exists", saved.getId());
            return false;
        }

        mailSenderPort.sendThresholdAlertEmail(user.getEmail(), post.getCaption(), buzzScore.getTotalScore(), saved.getAlertThreshold());
        String caption = (post.getCaption() == null || post.getCaption().isBlank()) ? "(キャプションなし)" : post.getCaption();
        notificationApplicationService.notify(user.getId(), NotificationType.THRESHOLD_ALERT,
                "設定したしきい値を超えました",
                "%s\n現在のBuzzScore: %.1f (しきい値: %.1f)".formatted(caption, buzzScore.getTotalScore(), saved.getAlertThreshold()),
                "/saved");
        saved.markAlertTriggered(OffsetDateTime.now());
        savedAnalysisRepository.save(saved);
        return true;
    }
}
