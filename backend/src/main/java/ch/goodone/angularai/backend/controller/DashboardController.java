package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.DashboardDTO;
import ch.goodone.angularai.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for dashboard data aggregation")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Get aggregated dashboard data")
    public DashboardDTO getDashboardData() {
        return dashboardService.getDashboardData();
    }
}
