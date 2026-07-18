package com.buzzanalysis.domain.preprocessing;

/** 投稿時刻の時間帯区分。 */
public enum TimeSlot {
    /** 0時〜5時 */
    LATE_NIGHT,
    /** 5時〜9時 */
    EARLY_MORNING,
    /** 9時〜12時 */
    MORNING,
    /** 12時〜17時 */
    AFTERNOON,
    /** 17時〜21時 */
    EVENING,
    /** 21時〜24時 */
    NIGHT
}
