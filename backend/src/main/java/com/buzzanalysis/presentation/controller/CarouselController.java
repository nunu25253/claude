package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.carousel.CarouselGenerationApplicationService;
import com.buzzanalysis.application.carousel.dto.CarouselDto;
import com.buzzanalysis.application.carousel.dto.CarouselGenerationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * カルーセル生成API（AIマーケティングOS Phase12）。Phase10の投稿企画を元に、AIがInstagram
 * カルーセル（1ページ目フック・中間ページ説明・最終ページCTA、2〜8ページ）を生成し永続化する。
 */
@RestController
@RequestMapping("/api/v1/carousels")
@Tag(name = "Carousels", description = "カルーセル生成AI（AIマーケティングOS Phase12）")
public class CarouselController {

    private final CarouselGenerationApplicationService carouselGenerationApplicationService;

    public CarouselController(CarouselGenerationApplicationService carouselGenerationApplicationService) {
        this.carouselGenerationApplicationService = carouselGenerationApplicationService;
    }

    @Operation(summary = "カルーセルの生成", description = "投稿企画IDから、AIがInstagramカルーセル(2〜8ページ)を生成する。")
    @PostMapping("/generate")
    public ResponseEntity<CarouselDto> generate(@RequestBody CarouselGenerationRequest request) {
        return ResponseEntity.ok(carouselGenerationApplicationService.generate(request));
    }

    @Operation(summary = "企画に紐づく生成済みカルーセルの取得", description = "指定した投稿企画IDに紐づくカルーセル一覧を取得する。")
    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<List<CarouselDto>> findByProposalId(@PathVariable UUID proposalId) {
        return ResponseEntity.ok(carouselGenerationApplicationService.findByProposalId(proposalId));
    }
}
