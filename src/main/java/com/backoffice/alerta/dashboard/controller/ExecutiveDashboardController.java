package com.backoffice.alerta.dashboard.controller;

import com.backoffice.alerta.dashboard.dto.ExecutiveDashboardResponse;
import com.backoffice.alerta.dashboard.service.ExecutiveDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller do Dashboard Executivo
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 * 
 * Visão consolidada para ADMIN e RISK_MANAGER
 */
@RestController
@RequestMapping("/risk/dashboard")
@Tag(name = "Executive Dashboard", description = "Dashboard executivo consolidado de alertas e risco")
public class ExecutiveDashboardController {
    
    private final ExecutiveDashboardService dashboardService;
    
    public ExecutiveDashboardController(ExecutiveDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    /**
     * GET /risk/dashboard/alerts-executive
     * 
     * Retorna visão executiva agregada do sistema de alertas e risco
     */
    @GetMapping("/alerts-executive")
    @Operation(
        summary = "Dashboard Executivo de Alertas",
        description = "Visão executiva agregada do sistema de alertas: métricas CI, alertas, projetos e regras em risco"
    )
    public ResponseEntity<ExecutiveDashboardResponse> getExecutiveDashboard() {
        ExecutiveDashboardResponse dashboard = dashboardService.getExecutiveDashboard();
        return ResponseEntity.ok(dashboard);
    }
}
