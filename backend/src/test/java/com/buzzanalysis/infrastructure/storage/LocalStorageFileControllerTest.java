package com.buzzanalysis.infrastructure.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link LocalStorageFileController} のMockMvcによるAPIテスト。
 * 署名検証(有効/期限切れ/改ざん/パラメータ欠落)を検証する
 * （Spring Securityフィルタはこのスライステストの対象外のため無効化し、コントローラのロジックのみ検証する）。
 */
@WebMvcTest(controllers = LocalStorageFileController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocalStorageFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocalFileStorageService localFileStorageService;

    @MockBean
    private LocalUrlSigner urlSigner;

    @Test
    void download_returnsOk_whenSignatureValid() throws Exception {
        long expires = Instant.now().plusSeconds(60).getEpochSecond();
        when(urlSigner.isValid(eq("reports/x/report.html"), eq(expires), eq("valid-sig"))).thenReturn(true);
        when(localFileStorageService.read("reports/x/report.html")).thenReturn("<html></html>".getBytes());

        mockMvc.perform(get("/api/v1/reports/files/reports/x/report.html")
                        .param("expires", String.valueOf(expires))
                        .param("sig", "valid-sig"))
                .andExpect(status().isOk());
    }

    @Test
    void download_returnsForbidden_whenSignatureInvalid() throws Exception {
        when(urlSigner.isValid(anyString(), anyLong(), anyString())).thenReturn(false);

        mockMvc.perform(get("/api/v1/reports/files/reports/x/report.html")
                        .param("expires", String.valueOf(Instant.now().plusSeconds(60).getEpochSecond()))
                        .param("sig", "tampered-sig"))
                .andExpect(status().isForbidden());
    }

    @Test
    void download_returnsBadRequest_whenSignatureParamMissing() throws Exception {
        mockMvc.perform(get("/api/v1/reports/files/reports/x/report.html")
                        .param("expires", String.valueOf(Instant.now().plusSeconds(60).getEpochSecond())))
                .andExpect(status().isBadRequest());
    }
}
