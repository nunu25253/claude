package com.buzzanalysis.domain.preprocessing;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;

/** 投稿日時から時間帯・曜日・週末フラグを解析する。 */
public class PostingTimeAnalyzer {

    public PostingTimeInfo analyze(OffsetDateTime publishedAt) {
        if (publishedAt == null) {
            return null;
        }
        int hour = publishedAt.getHour();
        DayOfWeek dayOfWeek = publishedAt.getDayOfWeek();
        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        return new PostingTimeInfo(hour, dayOfWeek, isWeekend, timeSlotOf(hour));
    }

    private TimeSlot timeSlotOf(int hour) {
        if (hour < 5) {
            return TimeSlot.LATE_NIGHT;
        }
        if (hour < 9) {
            return TimeSlot.EARLY_MORNING;
        }
        if (hour < 12) {
            return TimeSlot.MORNING;
        }
        if (hour < 17) {
            return TimeSlot.AFTERNOON;
        }
        if (hour < 21) {
            return TimeSlot.EVENING;
        }
        return TimeSlot.NIGHT;
    }
}
