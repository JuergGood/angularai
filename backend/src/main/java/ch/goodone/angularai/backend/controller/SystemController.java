package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.SystemInfoDTO;
import ch.goodone.angularai.backend.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final Environment environment;
    private final SystemSettingService systemSettingService;
    
    @Value("${application.version:unknown}")
    private String backendVersion;

    @Value("${frontend.version:unknown}")
    private String frontendVersion;

    @Value("${app.landing.message.en:Welcome to GoodOne!}")
    private String landingMessageEn;

    @Value("${app.landing.message.de-ch:Willkommen bei GoodOne!}")
    private String landingMessageDeCh;

    @Value("${google.recaptcha.1.site.key}")
    private String siteKey1;
    @Value("${google.recaptcha.2.site.key}")
    private String siteKey2;
    @Value("${google.recaptcha.3.site.key}")
    private String siteKey3;

    public SystemController(Environment environment, SystemSettingService systemSettingService) {
        this.environment = environment;
        this.systemSettingService = systemSettingService;
    }

    @GetMapping("/info")
    public ResponseEntity<SystemInfoDTO> getSystemInfo() {
        String mode = "Default";
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        
        if (activeProfiles.contains("postgres")) {
            mode = "Postgres";
        } else if (activeProfiles.contains("h2-file")) {
            mode = "H2 (File)";
        } else if (activeProfiles.contains("h2-mem")) {
            mode = "H2 (Memory)";
        } else if (activeProfiles.contains("h2")) {
            mode = "H2";
        }

        Locale locale = LocaleContextHolder.getLocale();
        String landingMessage = null;
        
        boolean enabled = systemSettingService.isLandingMessageEnabled();
        
        if (enabled) {
            if ("de".equals(locale.getLanguage())) {
                landingMessage = landingMessageDeCh;
            } else {
                landingMessage = landingMessageEn;
            }

            if (landingMessage == null || landingMessage.isBlank()) {
                landingMessage = landingMessageEn;
            }
        }

        return ResponseEntity.ok(new SystemInfoDTO(backendVersion, frontendVersion, mode, landingMessage));
    }

    @GetMapping("/recaptcha-site-key")
    public ResponseEntity<String> getRecaptchaSiteKey() {
        int index = systemSettingService.getRecaptchaConfigIndex();
        String siteKey = switch (index) {
            case 2 -> siteKey2;
            case 3 -> siteKey3;
            default -> siteKey1;
        };
        return ResponseEntity.ok(siteKey);
    }
}
