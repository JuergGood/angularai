package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.ActionLogDTO;
import ch.goodone.angularai.backend.service.ActionLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            Pageable pageable,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return actionLogService.getLogs(pageable, type, startDate, endDate);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearLogs() {
        actionLogService.clearLogs();
        return ResponseEntity.noContent().build();
    }
}
