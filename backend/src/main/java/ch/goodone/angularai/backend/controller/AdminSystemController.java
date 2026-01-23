package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.service.ActionLogService;
import ch.goodone.angularai.backend.service.IpLocationService;
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
    private final IpLocationService ipLocationService;

    public AdminSystemController(SystemSettingService systemSettingService, ActionLogService actionLogService, IpLocationService ipLocationService) {
        this.systemSettingService = systemSettingService;
        this.actionLogService = actionLogService;
        this.ipLocationService = ipLocationService;
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

    @GetMapping("/geolocation/test")
    public ResponseEntity<IpLocationService.GeoLocation> testGeolocation(@RequestParam String ip) {
        return ResponseEntity.ok(ipLocationService.lookup(ip));
    }
}
