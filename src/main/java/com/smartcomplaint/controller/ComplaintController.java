package com.smartcomplaint.controller;

import com.smartcomplaint.dto.ApiResponse;
import com.smartcomplaint.dto.ComplaintRequest;
import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.Role;
import com.smartcomplaint.model.User;
import com.smartcomplaint.service.ComplaintService;
import com.smartcomplaint.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final UserService userService;

    public ComplaintController(ComplaintService complaintService, UserService userService) {
        this.complaintService = complaintService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) {
            return null;
        }
        return userService.findById(userId).orElse(null);
    }

    /**
     * Submit a new complaint (Student action).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Complaint>> submitComplaint(
            @Valid @RequestBody ComplaintRequest req,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please login to submit a complaint"));
        }

        Complaint created = complaintService.createComplaint(req, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Complaint submitted successfully", created));
    }

    /**
     * Get complaints submitted by the logged-in student.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Complaint>>> getMyComplaints(HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please login to view your complaints"));
        }

        List<Complaint> complaints = complaintService.getComplaintsByStudent(user);
        return ResponseEntity.ok(ApiResponse.ok("Complaints retrieved", complaints));
    }

    /**
     * Get statistics for the logged-in student dashboard.
     */
    @GetMapping("/my/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getMyStats(HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please login to view stats"));
        }

        return ResponseEntity.ok(ApiResponse.ok("Stats retrieved", complaintService.getStudentStats(user)));
    }

    /**
     * View specific complaint details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Complaint>> getComplaintDetails(
            @PathVariable Long id,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please login to view complaint details"));
        }

        try {
            Complaint complaint = complaintService.getComplaintById(id);
            // Students can only view their own complaints; Admins can view any complaint
            if (user.getRole() != Role.ADMIN && !complaint.getStudent().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied: You can only view your own complaints"));
            }

            return ResponseEntity.ok(ApiResponse.ok("Complaint details retrieved", complaint));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
