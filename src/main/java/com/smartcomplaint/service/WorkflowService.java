package com.smartcomplaint.service;

import com.smartcomplaint.dto.ApprovalDecisionDto;
import com.smartcomplaint.dto.ApprovalRequestDto;
import com.smartcomplaint.dto.CommentRequest;
import com.smartcomplaint.model.*;
import com.smartcomplaint.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkflowService {

    private final ComplaintRepository complaintRepository;
    private final EmployeeRepository employeeRepository;
    private final ComplaintAssignmentRepository assignmentRepository;
    private final ComplaintCommentRepository commentRepository;
    private final ComplaintHistoryRepository historyRepository;
    private final ApprovalRepository approvalRepository;
    private final NotificationRepository notificationRepository;
    private final AuditService auditService;

    public WorkflowService(ComplaintRepository complaintRepository,
                           EmployeeRepository employeeRepository,
                           ComplaintAssignmentRepository assignmentRepository,
                           ComplaintCommentRepository commentRepository,
                           ComplaintHistoryRepository historyRepository,
                           ApprovalRepository approvalRepository,
                           NotificationRepository notificationRepository,
                           AuditService auditService) {
        this.complaintRepository = complaintRepository;
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.approvalRepository = approvalRepository;
        this.notificationRepository = notificationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ComplaintAssignment assignComplaint(Long complaintId, Long employeeId, String notes, User assignedBy) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        ComplaintAssignment assignment = new ComplaintAssignment(complaint, employee, assignedBy, notes);
        assignment = assignmentRepository.save(assignment);

        // Update complaint
        complaint.setAssignedStaff(employee.getName() + " (" + employee.getDesignation() + ")");
        if (complaint.getStatus() == ComplaintStatus.PENDING) {
            complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        }
        if (complaint.getDepartment() == null && employee.getDepartment() != null) {
            complaint.setDepartment(employee.getDepartment());
        }
        complaintRepository.save(complaint);

        // Update employee assigned tasks
        employee.setAssignedTasksCount(employee.getAssignedTasksCount() + 1);
        employeeRepository.save(employee);

        // Audit history
        historyRepository.save(new ComplaintHistory(
                complaintId,
                assignedBy,
                "ASSIGNED",
                "None",
                employee.getName(),
                notes != null ? notes : "Assigned to " + employee.getName()
        ));

        auditService.log(assignedBy, "ASSIGN_TICKET", "Complaint#" + complaintId, null,
                "Assigned to " + employee.getName() + " (" + employee.getDesignation() + ")");

        // Notify student & employee
        if (complaint.getStudent() != null) {
            notificationRepository.save(new Notification(
                    complaint.getStudent(),
                    "Technician Assigned",
                    "Technician " + employee.getName() + " has been assigned to ticket #" + complaintId,
                    "ASSIGNED",
                    complaintId
            ));
        }

        return assignment;
    }

    @Transactional
    public Complaint escalateComplaint(Long complaintId, String reason, User user) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));

        complaint.setIsEscalated(true);
        if (complaint.getPriority() != Priority.URGENT) {
            complaint.setPriority(Priority.HIGH);
        }
        complaint = complaintRepository.save(complaint);

        historyRepository.save(new ComplaintHistory(
                complaintId,
                user,
                "ESCALATED",
                "Normal",
                "Escalated",
                reason != null ? reason : "Ticket escalated to department head"
        ));

        auditService.log(user, "ESCALATE_TICKET", "Complaint#" + complaintId, null,
                reason != null ? reason : "Ticket escalated to department head");

        return complaint;
    }

    @Transactional
    public Approval requestApproval(Long complaintId, ApprovalRequestDto req, User requester) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));

        Approval approval = new Approval(complaint, requester, req.getApprovalType(), req.getAmount(), req.getReason());
        approval = approvalRepository.save(approval);

        complaint.setCostEstimate(req.getAmount());
        complaintRepository.save(complaint);

        historyRepository.save(new ComplaintHistory(
                complaintId,
                requester,
                "APPROVAL_REQUESTED",
                "None",
                req.getApprovalType() + " ($" + req.getAmount() + ")",
                req.getReason()
        ));

        auditService.log(requester, "REQUEST_APPROVAL", "Complaint#" + complaintId, null,
                req.getApprovalType() + " (" + req.getAmount() + "): " + req.getReason());

        return approval;
    }

    @Transactional
    public Approval decideApproval(Long approvalId, ApprovalDecisionDto decision, User approver) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + approvalId));

        approval.setStatus(decision.getStatus());
        approval.setComments(decision.getComments());
        approval.setApprover(approver);
        approval.setDecidedAt(LocalDateTime.now());
        approval = approvalRepository.save(approval);

        historyRepository.save(new ComplaintHistory(
                approval.getComplaint().getId(),
                approver,
                "APPROVAL_" + decision.getStatus(),
                "PENDING",
                decision.getStatus(),
                decision.getComments()
        ));

        auditService.log(approver, "DECIDE_APPROVAL", "Complaint#" + approval.getComplaint().getId(), null,
                decision.getStatus() + ": " + decision.getComments());

        if (approval.getRequester() != null) {
            notificationRepository.save(new Notification(
                    approval.getRequester(),
                    "Approval " + decision.getStatus(),
                    "Approval request for complaint #" + approval.getComplaint().getId() + " was " + decision.getStatus().toLowerCase(),
                    "APPROVAL_REQ",
                    approval.getComplaint().getId()
            ));
        }

        return approval;
    }

    @Transactional
    public ComplaintComment addComment(Long complaintId, CommentRequest req, User author) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));

        ComplaintComment comment = new ComplaintComment(complaint, author, req.getMessage().trim(), req.isInternal());
        comment = commentRepository.save(comment);

        historyRepository.save(new ComplaintHistory(
                complaintId,
                author,
                req.isInternal() ? "INTERNAL_NOTE_ADDED" : "COMMENT_ADDED",
                "",
                author.getName() + ": " + (req.getMessage().length() > 30 ? req.getMessage().substring(0, 30) + "..." : req.getMessage()),
                req.isInternal() ? "Internal note" : "Public comment"
        ));

        auditService.log(author, req.isInternal() ? "ADD_INTERNAL_NOTE" : "ADD_COMMENT",
                "Complaint#" + complaintId, null,
                req.getMessage().length() > 60 ? req.getMessage().substring(0, 60) + "..." : req.getMessage());

        return comment;
    }

    @Transactional(readOnly = true)
    public List<ComplaintComment> getComments(Long complaintId, boolean includeInternal) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));
        if (includeInternal) {
            return commentRepository.findByComplaintOrderByCreatedAtAsc(complaint);
        } else {
            return commentRepository.findByComplaintAndIsInternalFalseOrderByCreatedAtAsc(complaint);
        }
    }

    @Transactional(readOnly = true)
    public List<ComplaintHistory> getHistory(Long complaintId) {
        return historyRepository.findByComplaintIdOrderByTimestampDesc(complaintId);
    }

    @Transactional(readOnly = true)
    public List<ComplaintAssignment> getAssignments(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));
        return assignmentRepository.findByComplaintOrderByAssignedAtDesc(complaint);
    }

    @Transactional(readOnly = true)
    public List<Approval> getApprovals(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));
        return approvalRepository.findByComplaintOrderByRequestedAtDesc(complaint);
    }

    @Transactional(readOnly = true)
    public List<Approval> getPendingApprovals() {
        return approvalRepository.findByStatusOrderByRequestedAtDesc("PENDING");
    }
}

