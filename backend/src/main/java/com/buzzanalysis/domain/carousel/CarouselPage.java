package com.buzzanalysis.domain.carousel;

/** Instagramカルーセルの1ページ分（Phase12）。{@code role}は位置から決定済みの検証済み値。 */
public record CarouselPage(int pageNumber, PageRole role, String headline, String bodyText,
                            String visualDirection) {
}
