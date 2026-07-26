package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.proposal.ProposalGenerationApplicationService;
import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link ProposalController} のMockMvcによるAPIテスト。
 * postIds が null/空配列の場合に {@link NullPointerException} 起因の500ではなく、
 * {@code @Valid}(postIdsへの{@code @NotEmpty})によって400が返ることを検証する回帰テスト。
 */
@WebMvcTest(ProposalController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProposalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProposalGenerationApplicationService proposalGenerationApplicationService;

    private static MockHttpServletRequestBuilder authenticatedPost(String uri) {
        return post(uri).principal(new UsernamePasswordAuthenticationToken(UUID.randomUUID().toString(), null));
    }

    @Test
    void generate_returns400_whenPostIdsIsMissing() throws Exception {
        mockMvc.perform(authenticatedPost("/api/v1/proposals/generate")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_returns400_whenPostIdsIsEmptyArray() throws Exception {
        mockMvc.perform(authenticatedPost("/api/v1/proposals/generate")
                        .contentType("application/json")
                        .content("{\"postIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generate_returns200_whenPostIdsIsPresent() throws Exception {
        ContentProposalDto proposal = new ContentProposalDto(UUID.randomUUID(), UUID.randomUUID(), 1, "企画",
                null, null, null, null, null, null, null, OffsetDateTime.now());
        when(proposalGenerationApplicationService.generate(any(), any())).thenReturn(List.of(proposal));

        mockMvc.perform(authenticatedPost("/api/v1/proposals/generate")
                        .contentType("application/json")
                        .content("{\"postIds\":[\"" + UUID.randomUUID() + "\"]}"))
                .andExpect(status().isOk());
    }
}
