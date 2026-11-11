package com.smartcomplaint.service;

import com.smartcomplaint.dto.ErpDashboardDto;
import com.smartcomplaint.model.*;
import com.smartcomplaint.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AnalyticsService {

    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ApprovalRepository approvalRepository;
    private final ComplaintHistoryRepository historyRepository;

    public AnalyticsService(ComplaintRepository complaintRepository,
                            DepartmentRepository departmentRepository,
                            EmployeeRepository employeeRepository,
                            ApprovalRepository approvalRepository,
                            ComplaintHistoryRepository historyRepository) {
        this.complaintRepository = complaintRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.approvalRepository = approvalRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public ErpDashboardDto getErpDashboardData() {
        ErpDashboardDto dto = new ErpDashboardDto();

        long total = complaintRepository.count();
        long pending = complaintRepository.countByStatus(ComplaintStatus.PENDING);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long overdue = complaintRepository.countOverdueComplaints();
        long escalated = complaintRepository.countByIsEscalatedTrue();
        long totalEmp = employeeRepository.count();
        long totalDepts = departmentRepository.count();
        long pendingApprovals = approvalRepository.countByStatus("PENDING");

        dto.setTotalComplaints(total);
        dto.setOpenComplaints(pending);
        dto.setInProgressComplaints(inProgress);
        dto.setResolvedComplaints(resolved);
        dto.setOverdueComplaints(overdue);
        dto.setEscalatedComplaints(escalated);
        dto.setTotalEmployees(totalEmp);
        dto.setTotalDepartments(totalDepts);
        dto.setPendingApprovals(pendingApprovals);

        // Calculate SLA compliance rate
        double compliance = total > 0 ? ((double) (total - overdue) / total) * 100.0 : 100.0;
        dto.setSlaComplianceRate(Math.round(compliance * 10.0) / 10.0);

        // Category breakdown
        Map<String, Long> catDist = new LinkedHashMap<>();
        for (Category c : Category.values()) {
            long count = complaintRepository.findByCategoryOrderByCreatedAtDesc(c).size();
            if (count > 0) {
                catDist.put(c.name(), count);
            }
        }
        dto.setCategoryDistribution(catDist);

        // Priority breakdown
        Map<String, Long> prioDist = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            prioDist.put(p.name(), 0L);
        }
        for (Complaint comp : complaintRepository.findAll()) {
            Priority p = comp.getPriority() != null ? comp.getPriority() : Priority.MEDIUM;
            prioDist.put(p.name(), prioDist.getOrDefault(p.name(), 0L) + 1);
        }
        dto.setPriorityDistribution(prioDist);

        // Department performance
        List<ErpDashboardDto.DepartmentStatDto> deptStats = new ArrayList<>();
        for (Department d : departmentRepository.findAll()) {
            long dTotal = complaintRepository.countByDepartment(d);
            long dResolved = complaintRepository.countByDepartmentAndStatus(d, ComplaintStatus.RESOLVED);
            long dPending = complaintRepository.countByDepartmentAndStatus(d, ComplaintStatus.PENDING);
            long dEmp = employeeRepository.countByDepartment(d);
            deptStats.add(new ErpDashboardDto.DepartmentStatDto(
                    d.getId(),
                    d.getCode(),
                    d.getName(),
                    dTotal,
                    dResolved,
                    dPending,
                    dEmp,
                    d.getBudget() != null ? d.getBudget() : 0.0
            ));
        }
        dto.setDepartmentPerformance(deptStats);

        // Recent activity
        List<ErpDashboardDto.RecentActivityDto> activities = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
        for (ComplaintHistory h : historyRepository.findAll()) {
            String user = h.getChangedBy() != null ? h.getChangedBy().getName() : "System";
            activities.add(new ErpDashboardDto.RecentActivityDto(
                    h.getId(),
                    h.getAction(),
                    h.getNotes() != null ? h.getNotes() : (h.getAction() + " on ticket #" + h.getComplaintId()),
                    user,
                    h.getTimestamp() != null ? h.getTimestamp().format(dtf) : "Recent"
            ));
        }
        // Keep top 10 most recent
        activities.sort((a, b) -> b.getId().compareTo(a.getId()));
        if (activities.size() > 10) {
            activities = activities.subList(0, 10);
        }
        dto.setRecentActivities(activities);

        return dto;
    }

    @Transactional(readOnly = true)
    public String generateComplaintsCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ticket ID,Title,Category,Department,Priority,Status,Resident,Location,Assigned Staff,Created At,Resolved At,Escalated,Estimated Cost\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Complaint c : complaintRepository.findAllByOrderByCreatedAtDesc()) {
            sb.append(c.getId()).append(",");
            sb.append("\"").append(c.getTitle().replace("\"", "\"\"")).append("\",");
            sb.append(c.getCategory()).append(",");
            sb.append(c.getDepartment() != null ? c.getDepartment().getCode() : "N/A").append(",");
            sb.append(c.getPriority()).append(",");
            sb.append(c.getStatus()).append(",");
            sb.append("\"").append(c.getStudent() != null ? c.getStudent().getName() : "Anonymous").append("\",");
            sb.append("\"").append(c.getLocation() != null ? c.getLocation().replace("\"", "\"\"") : "").append("\",");
            sb.append("\"").append(c.getAssignedStaff() != null ? c.getAssignedStaff().replace("\"", "\"\"") : "").append("\",");
            sb.append(c.getCreatedAt() != null ? c.getCreatedAt().format(dtf) : "").append(",");
            sb.append(c.getResolvedAt() != null ? c.getResolvedAt().format(dtf) : "").append(",");
            sb.append(c.getIsEscalated() ? "YES" : "NO").append(",");
            sb.append(c.getCostEstimate()).append("\n");
        }

        return sb.toString();
    }
}

