package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.post.BuzzScoreHistoryApplicationService;
import com.buzzanalysis.application.post.PostAnalysisApplicationService;
import com.buzzanalysis.application.post.PostSearchApplicationService;
import com.buzzanalysis.application.post.dto.AnalyzePostCommand;
import com.buzzanalysis.application.post.dto.AnalyzePostResult;
import com.buzzanalysis.application.post.dto.BuzzScoreHistoryPointDto;
import com.buzzanalysis.application.post.dto.PostSearchQuery;
import com.buzzanalysis.application.post.dto.PostSearchResultDto;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.presentation.dto.request.AnalyzePostRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** 投稿分析・検索API。 */
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Posts", description = "投稿URL分析・投稿検索")
public class PostController {

    private final PostAnalysisApplicationService postAnalysisApplicationService;
    private final PostSearchApplicationService postSearchApplicationService;
    private final BuzzScoreHistoryApplicationService buzzScoreHistoryApplicationService;

    public PostController(PostAnalysisApplicationService postAnalysisApplicationService,
                           PostSearchApplicationService postSearchApplicationService,
                           BuzzScoreHistoryApplicationService buzzScoreHistoryApplicationService) {
        this.postAnalysisApplicationService = postAnalysisApplicationService;
        this.postSearchApplicationService = postSearchApplicationService;
        this.buzzScoreHistoryApplicationService = buzzScoreHistoryApplicationService;
    }

    @Operation(summary = "投稿URL分析",
            description = "投稿URLを受け取り、公開データ取得→AI分析→BuzzScore算出→改善案・類似投稿提案を返す")
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzePostResult> analyze(Authentication authentication, @Valid @RequestBody AnalyzePostRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        AnalyzePostResult result = postAnalysisApplicationService.analyze(new AnalyzePostCommand(request.postUrl(), userId));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "投稿検索", description = "キーワード/ハッシュタグ/アカウントによる検索。ページング・ソート対応")
    @GetMapping("/search")
    public ResponseEntity<PostSearchResultDto> search(
            @Parameter(description = "キャプション全文検索キーワード") @RequestParam(required = false) String keyword,
            @Parameter(description = "ハッシュタグ（#なし）") @RequestParam(required = false) String hashtag,
            @Parameter(description = "アカウントID") @RequestParam(required = false) UUID accountId,
            @Parameter(description = "プラットフォーム") @RequestParam(required = false) Platform platform,
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String sortBy = pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("publishedAt");
        boolean ascending = pageable.getSort().stream().findFirst().map(Sort.Order::isAscending).orElse(false);

        PostSearchQuery query = new PostSearchQuery(keyword, hashtag, accountId, platform,
                pageable.getPageNumber(), pageable.getPageSize(), sortBy, ascending);
        return ResponseEntity.ok(postSearchApplicationService.search(query));
    }

    @Operation(summary = "投稿のBuzzScore推移取得",
            description = "指定投稿を再分析するたびに記録される履歴を計算日時の昇順で返す")
    @GetMapping("/{postId}/buzz-score-history")
    public ResponseEntity<List<BuzzScoreHistoryPointDto>> buzzScoreHistory(@PathVariable UUID postId) {
        return ResponseEntity.ok(buzzScoreHistoryApplicationService.getHistory(postId));
    }
}
