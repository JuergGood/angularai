package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.service.ActionLogService;
import ch.goodone.angularai.backend.service.IpLocationService;
import ch.goodone.angularai.backend.service.SystemSettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@Tag(name = "Admin System Settings", description = "Endpoints for administrators to manage system settings")
public class AdminSystemController {
    
    private static final String ENABLED = "enabled";
    private static final String SETTING_CHANGED = "SETTING_CHANGED";

    private final SystemSettingService systemSettingService;
    private final ActionLogService actionLogService;
    private final IpLocationService ipLocationService;

    public AdminSystemController(SystemSettingService systemSettingService, ActionLogService actionLogService, IpLocationService ipLocationService) {
        this.systemSettingService = systemSettingService;
        this.actionLogService = actionLogService;
        this.ipLocationService = ipLocationService;
    }

    @GetMapping("/geolocation")
    public ResponseEntity<Map<String, Boolean>> getGeolocationEnabled() {
        return ResponseEntity.ok(Map.of(ENABLED, systemSettingService.isGeolocationEnabled()));
    }

    @PostMapping("/geolocation")
    public ResponseEntity<Void> setGeolocationEnabled(@RequestBody Map<String, Boolean> body, Authentication authentication) {
        boolean enabled = body.getOrDefault(ENABLED, false);
        systemSettingService.setGeolocationEnabled(enabled);
        actionLogService.log(authentication.getName(), SETTING_CHANGED, "Geolocation enabled set to: " + enabled);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recaptcha")
    public ResponseEntity<Map<String, Integer>> getRecaptchaConfigIndex() {
        return ResponseEntity.ok(Map.of("index", systemSettingService.getRecaptchaConfigIndex()));
    }

    @PostMapping("/recaptcha")
    public ResponseEntity<Void> setRecaptchaConfigIndex(@RequestBody Map<String, Integer> body, Authentication authentication) {
        int index = body.getOrDefault("index", 1);
        systemSettingService.setRecaptchaConfigIndex(index);
        actionLogService.log(authentication.getName(), SETTING_CHANGED, "reCAPTCHA config index set to: " + index);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/landing-message")
    public ResponseEntity<Map<String, Boolean>> getLandingMessageEnabled() {
        return ResponseEntity.ok(Map.of(ENABLED, systemSettingService.isLandingMessageEnabled()));
    }

    @PostMapping("/landing-message")
    public ResponseEntity<Void> setLandingMessageEnabled(@RequestBody Map<String, Boolean> body, Authentication authentication) {
        boolean enabled = body.getOrDefault(ENABLED, true);
        systemSettingService.setLandingMessageEnabled(enabled);
        actionLogService.log(authentication.getName(), SETTING_CHANGED, "Landing message enabled set to: " + enabled);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/geolocation/test")
    public ResponseEntity<IpLocationService.GeoLocation> testGeolocation(@RequestParam String ip) {
        return ResponseEntity.ok(ipLocationService.lookup(ip));
    }
}
