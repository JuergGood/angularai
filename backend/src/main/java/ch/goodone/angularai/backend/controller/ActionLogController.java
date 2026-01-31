package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.ActionLogDTO;
import ch.goodone.angularai.backend.service.ActionLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/logs")
@Tag(name = "Log Management", description = "Endpoints for administrators to view and manage system logs")
public class ActionLogController {

    private final ActionLogService actionLogService;

    public ActionLogController(ActionLogService actionLogService) {
        this.actionLogService = actionLogService;
    }

    @GetMapping
    public Page<ActionLogDTO> getLogs(
            org.springframework.security.core.Authentication authentication,
            Pageable pageable,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        if (authentication != null) {
            actionLogService.log(authentication.getName(), "LOGS_VIEWED", "User viewed action logs");
        }
        return actionLogService.getLogs(pageable, type, startDate, endDate);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearLogs() {
        actionLogService.clearLogs();
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ActionLogDTO createLog(@RequestBody ActionLogDTO logDTO) {
        return actionLogService.createLog(logDTO);
    }
}
