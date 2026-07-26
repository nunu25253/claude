package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.commonality.CommonalityAnalysisApplicationService;
import com.buzzanalysis.application.commonality.dto.CommonalityAnalysisResultDto;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link CommonalityAnalysisController} のMockMvcによるAPIテスト。
 * postIds が null/空配列の場合に {@link NullPointerException} 起因の500ではなく、
 * {@code @Valid}(postIdsへの{@code @NotEmpty})によって400が返ることを検証する回帰テスト。
 */
@WebMvcTest(CommonalityAnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommonalityAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommonalityAnalysisApplicationService commonalityAnalysisApplicationService;

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedPost(String uri) {
        return post(uri).principal(new UsernamePasswordAuthenticationToken(UUID.randomUUID().toString(), null));
    }

    @Test
    void analyze_returns400_whenPostIdsIsMissing() throws Exception {
        mockMvc.perform(authenticatedPost("/api/v1/commonality/analyze")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyze_returns400_whenPostIdsIsEmptyArray() throws Exception {
        mockMvc.perform(authenticatedPost("/api/v1/commonality/analyze")
                        .contentType("application/json")
                        .content("{\"postIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyze_returns200_whenPostIdsIsPresent() throws Exception {
        UUID postId = UUID.randomUUID();
        CommonalityAnalysisResultDto result = new CommonalityAnalysisResultDto(
                1, 1, List.of(), null, null, ContentFormat.SHORT_VIDEO, null, null, null, null, null);
        when(commonalityAnalysisApplicationService.analyze(any(), any())).thenReturn(result);

        mockMvc.perform(authenticatedPost("/api/v1/commonality/analyze")
                        .contentType("application/json")
                        .content("{\"postIds\":[\"" + postId + "\"]}"))
                .andExpect(status().isOk());
    }
}
