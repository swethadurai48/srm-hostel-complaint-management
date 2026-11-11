package com.smartcomplaint.controller;

import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.dto.ErpDashboardDto;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.AnalyticsService;
import com.smartcomplaint.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/erp")
public class ErpDashboardController {

    private final AnalyticsService analyticsService;
    private final UserService userService;

    public ErpDashboardController(AnalyticsService analyticsService, UserService userService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) return null;
        return userService.findById(userId).orElse(null);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ErpDashboardDto>> getDashboard(HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole().isReporter()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Staff or administrative privileges required for ERP Dashboard"));
        }

        ErpDashboardDto data = analyticsService.getErpDashboardData();
        return ResponseEntity.ok(ApiResponse.ok("ERP Dashboard data retrieved", data));
    }

    @GetMapping("/reports/export-csv")
    public void exportComplaintsCsv(HttpSession session, HttpServletResponse response) throws IOException {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole().isReporter()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        String csv = analyticsService.generateComplaintsCsv();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"srm_erp_complaints_report.csv\"");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}
