package com.smartcomplaint.controller;

import com.smartcomplaint.dto.*;
import com.smartcomplaint.model.*;
import com.smartcomplaint.service.UserService;
import com.smartcomplaint.service.WorkflowService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/erp")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final UserService userService;

    public WorkflowController(WorkflowService workflowService, UserService userService) {
        this.workflowService = workflowService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) return null;
        return userService.findById(userId).orElse(null);
    }

    @PostMapping("/complaints/{id}/assign")
    public ResponseEntity<ApiResponse<ComplaintAssignment>> assignComplaint(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequest req,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole().isReporter()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Staff access required"));
        }
        try {
            ComplaintAssignment assignment = workflowService.assignComplaint(id, req.getEmployeeId(), req.getNotes(), user);
            return ResponseEntity.ok(ApiResponse.ok("Complaint assigned successfully", assignment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/complaints/{id}/escalate")
    public ResponseEntity<ApiResponse<Complaint>> escalateComplaint(
            @PathVariable Long id,
            @Valid @RequestBody EscalationRequest req,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Login required"));
        }
        try {
            Complaint complaint = workflowService.escalateComplaint(id, req.getReason(), user);
            return ResponseEntity.ok(ApiResponse.ok("Complaint escalated successfully", complaint));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/complaints/{id}/approvals")
    public ResponseEntity<ApiResponse<Approval>> requestApproval(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequestDto req,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole().isReporter()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Staff access required"));
        }
        try {
            Approval approval = workflowService.requestApproval(id, req, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Approval requested", approval));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/approvals/{id}/decide")
    public ResponseEntity<ApiResponse<Approval>> decideApproval(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalDecisionDto decision,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null || !user.getRole().isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Manager or Admin access required"));
        }
        try {
            Approval approval = workflowService.decideApproval(id, decision, user);
            return ResponseEntity.ok(ApiResponse.ok("Approval decision saved", approval));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/approvals/pending")
    public ResponseEntity<ApiResponse<List<Approval>>> getPendingApprovals(HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole().isReporter()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Staff access required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Pending approvals retrieved", workflowService.getPendingApprovals()));
    }

    @GetMapping("/complaints/{id}/approvals")
    public ResponseEntity<ApiResponse<List<Approval>>> getComplaintApprovals(
            @PathVariable Long id,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Login required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Approvals retrieved", workflowService.getApprovals(id)));
    }

    @PostMapping("/complaints/{id}/comments")
    public ResponseEntity<ApiResponse<ComplaintComment>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest req,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Login required"));
        }
        // Reporters cannot submit internal notes
        if (req.isInternal() && user.getRole().isReporter()) {
            req.setInternal(false);
        }
        try {
            ComplaintComment comment = workflowService.addComment(id, req, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Comment added", comment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/complaints/{id}/comments")
    public ResponseEntity<ApiResponse<List<ComplaintComment>>> getComments(
            @PathVariable Long id,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Login required"));
        }
        boolean isStaff = !user.getRole().isReporter();
        return ResponseEntity.ok(ApiResponse.ok("Comments retrieved", workflowService.getComments(id, isStaff)));
    }

    @GetMapping("/complaints/{id}/history")
    public ResponseEntity<ApiResponse<List<ComplaintHistory>>> getHistory(
            @PathVariable Long id,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Login required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("History retrieved", workflowService.getHistory(id)));
    }

    @GetMapping("/complaints/{id}/assignments")
    public ResponseEntity<ApiResponse<List<ComplaintAssignment>>> getAssignments(
            @PathVariable Long id,
            HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Login required"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Assignments retrieved", workflowService.getAssignments(id)));
    }
}
