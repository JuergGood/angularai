package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.service.ActionLogService;
import ch.goodone.angularai.backend.service.SystemSettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@Tag(name = "Admin System Settings", description = "Endpoints for administrators to manage system settings")
public class AdminSystemController {

    private final SystemSettingService systemSettingService;
    private final ActionLogService actionLogService;

    public AdminSystemController(SystemSettingService systemSettingService, ActionLogService actionLogService) {
        this.systemSettingService = systemSettingService;
        this.actionLogService = actionLogService;
    }

    @GetMapping("/geolocation")
    public ResponseEntity<Map<String, Boolean>> getGeolocationEnabled() {
        return ResponseEntity.ok(Map.of("enabled", systemSettingService.isGeolocationEnabled()));
    }

    @PostMapping("/geolocation")
    public ResponseEntity<Void> setGeolocationEnabled(@RequestBody Map<String, Boolean> body, Authentication authentication) {
        boolean enabled = body.getOrDefault("enabled", false);
        systemSettingService.setGeolocationEnabled(enabled);
        actionLogService.log(authentication.getName(), "SETTING_CHANGED", "Geolocation enabled set to: " + enabled);
        return ResponseEntity.ok().build();
    }
}
