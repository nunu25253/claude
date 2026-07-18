package com.buzzanalysis.domain.preprocessing;

import java.time.DayOfWeek;

/**
 * 投稿時間解析の結果。
 *
 * @param hour       投稿時刻の時（0〜23）
 * @param dayOfWeek  投稿曜日
 * @param isWeekend  土日の投稿かどうか
 * @param timeSlot   時間帯区分
 */
public record PostingTimeInfo(int hour, DayOfWeek dayOfWeek, boolean isWeekend, TimeSlot timeSlot) {
}
