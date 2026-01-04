package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.SystemInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final Environment environment;
    
    @Value("${application.version:unknown}")
    private String version;

    public SystemController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/info")
    public ResponseEntity<SystemInfoDTO> getSystemInfo() {
        String mode = "Default";
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        
        if (activeProfiles.contains("postgres")) {
            mode = "Postgres";
        } else if (activeProfiles.contains("h2")) {
            mode = "H2";
        }

        return ResponseEntity.ok(new SystemInfoDTO(version, mode));
    }
}
