package ch.goodone.angularai.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SystemSettingService systemSettingService;

    @InjectMocks
    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(captchaService, "siteKey1", "site1");
        ReflectionTestUtils.setField(captchaService, "secretKey1", "secret1");
        ReflectionTestUtils.setField(captchaService, "siteKey2", "site2");
        ReflectionTestUtils.setField(captchaService, "secretKey2", "secret2");
        ReflectionTestUtils.setField(captchaService, "siteKey3", "site3");
        ReflectionTestUtils.setField(captchaService, "secretKey3", "secret3");
    }

    @Test
    void verify_ShouldReturnTrue_WhenDisabled() {
        ReflectionTestUtils.setField(captchaService, "secretKey1", "disabled");
        when(systemSettingService.getRecaptchaConfigIndex()).thenReturn(1);

        assertTrue(captchaService.verify("token"));
    }

    @Test
    void verify_ShouldReturnFalse_WhenTokenMissing() {
        when(systemSettingService.getRecaptchaConfigIndex()).thenReturn(1);

        assertFalse(captchaService.verify(null));
        assertFalse(captchaService.verify(""));
    }

    @Test
    void verifyLegacy_ShouldReturnTrue_WhenSuccessful() {
        when(systemSettingService.getRecaptchaConfigIndex()).thenReturn(1);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class), anyMap()))
                .thenReturn(response);

        assertTrue(captchaService.verify("token"));
    }

    @Test
    void verifyLegacy_ShouldReturnFalse_WhenFailed() {
        when(systemSettingService.getRecaptchaConfigIndex()).thenReturn(2);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class), anyMap()))
                .thenReturn(response);

        assertFalse(captchaService.verify("token"));
    }

    @Test
    void verifyEnterprise_ShouldReturnTrue_WhenValid() {
        ReflectionTestUtils.setField(captchaService, "projectId1", "project1");
        ReflectionTestUtils.setField(captchaService, "apiKey1", "api1");
        when(systemSettingService.getRecaptchaConfigIndex()).thenReturn(1);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> tokenProperties = new HashMap<>();
        tokenProperties.put("valid", true);
        response.put("tokenProperties", tokenProperties);

        when(restTemplate.postForObject(anyString(), anyMap(), eq(Map.class)))
                .thenReturn(response);

        assertTrue(captchaService.verify("token"));
    }

    @Test
    void verifyEnterprise_ShouldReturnFalse_WhenInvalid() {
        ReflectionTestUtils.setField(captchaService, "projectId3", "project3");
        ReflectionTestUtils.setField(captchaService, "apiKey3", "api3");
        when(systemSettingService.getRecaptchaConfigIndex()).thenReturn(3);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> tokenProperties = new HashMap<>();
        tokenProperties.put("valid", false);
        tokenProperties.put("invalidReason", "DUPLICATE");
        response.put("tokenProperties", tokenProperties);

        when(restTemplate.postForObject(anyString(), anyMap(), eq(Map.class)))
                .thenReturn(response);

        assertFalse(captchaService.verify("token"));
    }

    @Test
    void verify_ShouldHandleExceptions() {
        when(systemSettingService.getRecaptchaConfigIndex()).thenReturn(1);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class), anyMap()))
                .thenThrow(new RuntimeException("API error"));

        assertFalse(captchaService.verify("token"));
    }
}
