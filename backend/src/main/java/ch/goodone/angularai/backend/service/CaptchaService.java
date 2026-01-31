package ch.goodone.angularai.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaService.class);

    private final RestTemplate restTemplate;
    private final SystemSettingService systemSettingService;

    @Value("${google.recaptcha.1.site.key:}")
    private String siteKey1;
    @Value("${google.recaptcha.1.secret.key:}")
    private String secretKey1;
    @Value("${google.recaptcha.1.project.id:}")
    private String projectId1;
    @Value("${google.recaptcha.1.api.key:}")
    private String apiKey1;

    @Value("${google.recaptcha.2.site.key:}")
    private String siteKey2;
    @Value("${google.recaptcha.2.secret.key:}")
    private String secretKey2;
    @Value("${google.recaptcha.2.project.id:}")
    private String projectId2;
    @Value("${google.recaptcha.2.api.key:}")
    private String apiKey2;

    @Value("${google.recaptcha.3.site.key:}")
    private String siteKey3;
    @Value("${google.recaptcha.3.secret.key:}")
    private String secretKey3;
    @Value("${google.recaptcha.3.project.id:}")
    private String projectId3;
    @Value("${google.recaptcha.3.api.key:}")
    private String apiKey3;

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final String ENTERPRISE_VERIFY_URL_TEMPLATE = "https://recaptchaenterprise.googleapis.com/v1/projects/%s/assessments?key=%s";

    public CaptchaService(RestTemplate restTemplate, SystemSettingService systemSettingService) {
        this.restTemplate = restTemplate;
        this.systemSettingService = systemSettingService;
    }

    private String getActiveSecret() {
        int index = systemSettingService.getRecaptchaConfigIndex();
        return switch (index) {
            case 2 -> secretKey2;
            case 3 -> secretKey3;
            default -> secretKey1;
        };
    }

    private String getActiveProjectId() {
        int index = systemSettingService.getRecaptchaConfigIndex();
        return switch (index) {
            case 2 -> projectId2;
            case 3 -> projectId3;
            default -> projectId1;
        };
    }

    private String getActiveApiKey() {
        int index = systemSettingService.getRecaptchaConfigIndex();
        return switch (index) {
            case 2 -> apiKey2;
            case 3 -> apiKey3;
            default -> apiKey1;
        };
    }

    private String getActiveSiteKey() {
        int index = systemSettingService.getRecaptchaConfigIndex();
        return switch (index) {
            case 2 -> siteKey2;
            case 3 -> siteKey3;
            default -> siteKey1;
        };
    }

    public boolean verify(String token) {
        int index = systemSettingService.getRecaptchaConfigIndex();
        String secret = getActiveSecret();
        
        // For development/test environments where the key might be missing or set to a dummy value
        if ("disabled".equals(secret) || "dummy".equals(secret)) {
            logger.info("reCAPTCHA verification skipped (disabled/dummy) for config {}", index);
            return true;
        }

        if (token == null || token.isBlank()) {
            logger.warn("reCAPTCHA token is missing");
            return false;
        }

        String projectId = getActiveProjectId();
        String apiKey = getActiveApiKey();

        if (projectId != null && !projectId.isBlank() && !"dummy".equals(projectId) 
                && apiKey != null && !apiKey.isBlank() && !"dummy".equals(apiKey)) {
            return verifyEnterprise(token, projectId, apiKey);
        }

        return verifyLegacy(token, secret);
    }

    private boolean verifyLegacy(String token, String secret) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("secret", secret);
            body.put("response", token);

            Map<String, Object> response = restTemplate.postForObject(
                    RECAPTCHA_VERIFY_URL + "?secret={secret}&response={response}",
                    null,
                    Map.class,
                    body
            );

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return true;
            } else {
                logger.warn("reCAPTCHA legacy verification failed: {}", response);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error during reCAPTCHA legacy verification", e);
            return false;
        }
    }

    private boolean verifyEnterprise(String token, String projectId, String apiKey) {
        try {
            String url = String.format(ENTERPRISE_VERIFY_URL_TEMPLATE, projectId, apiKey);

            Map<String, Object> event = new HashMap<>();
            event.put("token", token);
            event.put("siteKey", getActiveSiteKey());

            Map<String, Object> body = new HashMap<>();
            body.put("event", event);

            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);

            if (response != null && response.get("tokenProperties") != null) {
                Map<String, Object> tokenProperties = (Map<String, Object>) response.get("tokenProperties");
                boolean valid = Boolean.TRUE.equals(tokenProperties.get("valid"));
                
                // For score-based assessments (Enterprise), you might also check response.get("riskAnalysis")
                // but since the user seems to be using v2-style checkbox with Enterprise, "valid" should suffice.
                
                if (valid) {
                    return true;
                } else {
                    logger.warn("reCAPTCHA Enterprise verification invalid: {}", tokenProperties.get("invalidReason"));
                }
            }
            logger.warn("reCAPTCHA Enterprise verification failed: {}", response);
            return false;
        } catch (Exception e) {
            logger.error("Error during reCAPTCHA Enterprise verification", e);
            return false;
        }
    }
}
