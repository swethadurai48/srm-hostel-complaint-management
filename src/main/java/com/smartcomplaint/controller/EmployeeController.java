package com.smartcomplaint.controller;

import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.dto.EmployeeRequest;
import com.smartcomplaint.model.Employee;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.EmployeeService;
import com.smartcomplaint.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/employees", "/api/erp/employees"})
public class EmployeeController {

    private final EmployeeService employeeService;
    private final UserService userService;

    public EmployeeController(EmployeeService employeeService, UserService userService) {
        this.employeeService = employeeService;
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
    public ResponseEntity<ApiResponse<List<Employee>>> getAllEmployees(HttpSession session) {
        if (getAuthenticatedStaff(session) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Staff access required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Employees retrieved", employeeService.getAllEmployees()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeById(@PathVariable Long id, HttpSession session) {
        if (getAuthenticatedStaff(session) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Staff access required"));
        }
        try {
            return ResponseEntity.ok(ApiResponse.ok("Employee retrieved", employeeService.getEmployeeById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByDepartment(@PathVariable Long departmentId, HttpSession session) {
        if (getAuthenticatedStaff(session) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Staff access required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Department employees retrieved", employeeService.getEmployeesByDepartment(departmentId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Employee>> createEmployee(@Valid @RequestBody EmployeeRequest req, HttpSession session) {
        User user = getAuthenticatedStaff(session);
        if (user == null || !user.getRole().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required"));
        }
        try {
            Employee emp = employeeService.createEmployee(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Employee created successfully", emp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest req, HttpSession session) {
        User user = getAuthenticatedStaff(session);
        if (user == null || !user.getRole().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required"));
        }
        try {
            Employee updated = employeeService.updateEmployee(id, req);
            return ResponseEntity.ok(ApiResponse.ok("Employee updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id, HttpSession session) {
        User user = getAuthenticatedStaff(session);
        if (user == null || !user.getRole().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Admin privileges required"));
        }
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(ApiResponse.ok("Employee deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
