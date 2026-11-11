package com.smartcomplaint.controller;

import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.dto.DepartmentRequest;
import com.smartcomplaint.model.Department;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.DepartmentService;
import com.smartcomplaint.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/departments", "/api/erp/departments"})
public class DepartmentController {

    private final DepartmentService departmentService;
    private final UserService userService;

    public DepartmentController(DepartmentService departmentService, UserService userService) {
        this.departmentService = departmentService;
        this.userService = userService;
    }

    private User getAuthenticatedStaff(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) return null;
        User user = userService.findById(userId).orElse(null);
        if (user == null || user.getRole().isReporter()) return null;
        return user;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments(HttpSession session) {
        return ResponseEntity.ok(ApiResponse.ok("Departments retrieved", departmentService.getAllDepartments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> getDepartmentById(@PathVariable Long id, HttpSession session) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Department retrieved", departmentService.getDepartmentById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Department>> createDepartment(@Valid @RequestBody DepartmentRequest req, HttpSession session) {
        User user = getAuthenticatedStaff(session);
        if (user == null || !user.getRole().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required"));
        }
        try {
            Department dept = departmentService.createDepartment(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Department created successfully", dept));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentRequest req, HttpSession session) {
        User user = getAuthenticatedStaff(session);
        if (user == null || !user.getRole().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required"));
        }
        try {
            Department updated = departmentService.updateDepartment(id, req);
            return ResponseEntity.ok(ApiResponse.ok("Department updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Department>> updateDepartmentStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        User user = getAuthenticatedStaff(session);
        if (user == null || !user.getRole().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required"));
        }
        try {
            Department updated = departmentService.updateDepartmentStatus(id, status);
            return ResponseEntity.ok(ApiResponse.ok("Department status updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id, HttpSession session) {
        User user = getAuthenticatedStaff(session);
        if (user == null || !user.getRole().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required"));
        }
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok(ApiResponse.ok("Department deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDepartmentStats(@PathVariable Long id, HttpSession session) {
        if (getAuthenticatedStaff(session) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Staff access required"));
        }
        try {
            return ResponseEntity.ok(ApiResponse.ok("Stats retrieved", departmentService.getDepartmentStats(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
