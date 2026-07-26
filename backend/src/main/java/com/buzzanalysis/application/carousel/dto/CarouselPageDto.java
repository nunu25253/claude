package com.buzzanalysis.application.carousel.dto;

import com.buzzanalysis.domain.carousel.CarouselPage;
import com.buzzanalysis.domain.carousel.PageRole;

/** {@link CarouselPage}（ドメイン）のapplication層向けDTO。 */
public record CarouselPageDto(int pageNumber, PageRole role, String headline, String bodyText,
                               String visualDirection) {
    public static CarouselPageDto from(CarouselPage p) {
        return new CarouselPageDto(p.pageNumber(), p.role(), p.headline(), p.bodyText(), p.visualDirection());
    }
}
