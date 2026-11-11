package com.smartcomplaint.service;

import com.smartcomplaint.dto.ComplaintRequest;
import com.smartcomplaint.model.*;
import com.smartcomplaint.repository.ComplaintHistoryRepository;
import com.smartcomplaint.repository.ComplaintRepository;
import com.smartcomplaint.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintHistoryRepository historyRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            DepartmentRepository departmentRepository,
                            ComplaintHistoryRepository historyRepository) {
        this.complaintRepository = complaintRepository;
        this.departmentRepository = departmentRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Submit a new complaint by a student.
     */
    @Transactional
    public Complaint createComplaint(ComplaintRequest req, User student) {
        Complaint complaint = new Complaint();
        complaint.setTitle(req.getTitle().trim());
        complaint.setDescription(req.getDescription().trim());
        complaint.setCategory(req.getCategory());
        
        // Build readable location if individual hostel fields are provided
        String loc = req.getLocation();
        if ((loc == null || loc.isBlank()) && req.getRoomNumber() != null) {
            StringBuilder sb = new StringBuilder();
            if (req.getHostel() != null && !req.getHostel().isBlank()) sb.append(req.getHostel()).append(", ");
            if (req.getBlock() != null && !req.getBlock().isBlank()) sb.append(req.getBlock()).append(", ");
            if (req.getFloor() != null && !req.getFloor().isBlank()) sb.append("Floor ").append(req.getFloor()).append(", ");
            sb.append("Room ").append(req.getRoomNumber());
            loc = sb.toString();
        }
        complaint.setLocation(loc != null ? loc.trim() : null);
        Priority prio = req.getPriority() != null ? req.getPriority() : Priority.MEDIUM;
        complaint.setPriority(prio);
        complaint.setHostel(req.getHostel());
        complaint.setBlock(req.getBlock());
        complaint.setFloor(req.getFloor());
        complaint.setRoomNumber(req.getRoomNumber());
        complaint.setAttachmentUrl(req.getAttachmentUrl());
        complaint.setCostEstimate(req.getCostEstimate() != null ? req.getCostEstimate() : 0.0);
        complaint.setStudent(student);
        complaint.setStatus(ComplaintStatus.PENDING);
        complaint.setIsEscalated(false);

        // Auto-assign Department
        if (req.getDepartmentId() != null) {
            departmentRepository.findById(req.getDepartmentId()).ifPresent(complaint::setDepartment);
        } else {
            String deptCode = switch (req.getCategory()) {
                case ELECTRICITY -> "ELEC";
                case WATER -> "PLUMB";
                case WIFI -> "NET";
                case CLEANING -> "HOUSE";
                case MAINTENANCE -> "MAINT";
                case HOSTEL -> "MESS";
                case TRANSPORT -> "SEC";
                default -> "MAINT";
            };
            departmentRepository.findByCode(deptCode).ifPresent(complaint::setDepartment);
        }

        // SLA calculation
        int slaHours = switch (prio) {
            case URGENT -> 4;
            case HIGH -> 12;
            case MEDIUM -> 24;
            case LOW -> 48;
        };
        complaint.setSlaDueDate(LocalDateTime.now().plusHours(slaHours));

        Complaint saved = complaintRepository.save(complaint);

        // Record history
        historyRepository.save(new ComplaintHistory(
                saved.getId(),
                student,
                "CREATED",
                "",
                "Created with priority " + prio.name(),
                "New ticket logged"
        ));

        return saved;
    }

    /**
     * Get complaints submitted by a specific student.
     */
    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByStudent(User student) {
        return complaintRepository.findByStudentOrderByCreatedAtDesc(student);
    }

    /**
     * Find complaint by ID.
     */
    @Transactional(readOnly = true)
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with id: " + id));
    }

    /**
     * Filter complaints for admin view by Category and/or Status.
     */
    @Transactional(readOnly = true)
    public List<Complaint> getFilteredComplaints(Category category, ComplaintStatus status) {
        if (category == null && status == null) {
            return complaintRepository.findAllByOrderByCreatedAtDesc();
        } else if (category != null && status == null) {
            return complaintRepository.findByCategoryOrderByCreatedAtDesc(category);
        } else if (category == null) {
            return complaintRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            return complaintRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, status);
        }
    }

    /**
     * Update complaint status (Admin action).
     */
    @Transactional
    public Complaint updateStatus(Long complaintId, ComplaintStatus newStatus) {
        return updateStatus(complaintId, newStatus, null);
    }

    /**
     * Update complaint status and assigned maintenance staff (Admin action).
     */
    @Transactional
    public Complaint updateStatus(Long complaintId, ComplaintStatus newStatus, String assignedStaff) {
        Complaint complaint = getComplaintById(complaintId);
        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setStatus(newStatus);
        if (assignedStaff != null) {
            complaint.setAssignedStaff(assignedStaff.trim());
        }
        Complaint saved = complaintRepository.save(complaint);

        historyRepository.save(new ComplaintHistory(
                complaintId,
                null,
                "STATUS_UPDATED",
                oldStatus.name(),
                newStatus.name(),
                "Status transitioned to " + newStatus.name() + (assignedStaff != null ? " with staff " + assignedStaff : "")
        ));

        return saved;
    }

    /**
     * Add or update admin response (Admin action).
     */
    @Transactional
    public Complaint addAdminResponse(Long complaintId, String responseText) {
        Complaint complaint = getComplaintById(complaintId);
        complaint.setAdminResponse(responseText.trim());
        Complaint saved = complaintRepository.save(complaint);

        historyRepository.save(new ComplaintHistory(
                complaintId,
                null,
                "ADMIN_RESPONSE",
                "",
                responseText.trim(),
                "Official administrative response added"
        ));

        return saved;
    }

    /**
     * Summary counts for Admin dashboard KPIs.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getAdminStats() {
        Map<String, Long> stats = new HashMap<>();
        long pending = complaintRepository.countByStatus(ComplaintStatus.PENDING);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long total = complaintRepository.count();

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("inProgress", inProgress);
        stats.put("resolved", resolved);
        return stats;
    }

    /**
     * Summary counts for Student dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStudentStats(User student) {
        Map<String, Long> stats = new HashMap<>();
        long total = complaintRepository.countByStudent(student);
        long pending = complaintRepository.countByStudentAndStatus(student, ComplaintStatus.PENDING);
        long inProgress = complaintRepository.countByStudentAndStatus(student, ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStudentAndStatus(student, ComplaintStatus.RESOLVED);

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("inProgress", inProgress);
        stats.put("resolved", resolved);
        return stats;
    }
}
