package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.infrastructure.external.platform.instagram.InstagramApiProperties;
import com.buzzanalysis.infrastructure.external.platform.tiktok.TikTokApiProperties;
import com.buzzanalysis.infrastructure.external.platform.x.XApiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** {@link SystemController} のMockMvcによるAPIテスト。 */
@WebMvcTest(SystemController.class)
@AutoConfigureMockMvc(addFilters = false)
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstagramApiProperties instagramApiProperties;

    @MockBean
    private TikTokApiProperties tikTokApiProperties;

    @MockBean
    private XApiProperties xApiProperties;

    @Test
    void getDataMode_returnsAllFalse_whenNoPlatformIsConfigured() throws Exception {
        mockMvc.perform(get("/api/v1/system/data-mode"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instagramLive").value(false))
                .andExpect(jsonPath("$.tiktokLive").value(false))
                .andExpect(jsonPath("$.xLive").value(false))
                .andExpect(jsonPath("$.anyPlatformLive").value(false));
    }

    @Test
    void getDataMode_returnsAnyPlatformLiveTrue_whenAtLeastOneIsConfigured() throws Exception {
        org.mockito.Mockito.when(instagramApiProperties.isConfigured()).thenReturn(true);

        mockMvc.perform(get("/api/v1/system/data-mode"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instagramLive").value(true))
                .andExpect(jsonPath("$.tiktokLive").value(false))
                .andExpect(jsonPath("$.xLive").value(false))
                .andExpect(jsonPath("$.anyPlatformLive").value(true));
    }
}
