package com.buzzanalysis.domain.carousel;

/**
 * カルーセルの1ページが担う役割（Phase12）。AIには判定させず、ページ配列内の位置から
 * コード側で機械的に決定する（先頭=HOOK、末尾=CTA、それ以外=EXPLANATION）。
 */
public enum PageRole {
    HOOK,
    EXPLANATION,
    CTA
}
