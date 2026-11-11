package com.smartcomplaint.dto;

import java.util.List;
import java.util.Map;

public class ErpDashboardDto {

    // Primary KPI Counters
    private long totalComplaints;
    private long openComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long overdueComplaints;
    private long escalatedComplaints;
    private long totalEmployees;
    private long totalDepartments;
    private long pendingApprovals;
    private double slaComplianceRate;

    // Breakdown Analytics
    private Map<String, Long> categoryDistribution;
    private Map<String, Long> priorityDistribution;
    private List<DepartmentStatDto> departmentPerformance;
    private List<RecentActivityDto> recentActivities;

    public ErpDashboardDto() {
    }

    public static class DepartmentStatDto {
        private Long departmentId;
        private String code;
        private String name;
        private long totalComplaints;
        private long resolvedComplaints;
        private long pendingComplaints;
        private long employeeCount;
        private double budget;

        public DepartmentStatDto() {
        }

        public DepartmentStatDto(Long departmentId, String code, String name, long totalComplaints, long resolvedComplaints, long pendingComplaints, long employeeCount, double budget) {
            this.departmentId = departmentId;
            this.code = code;
            this.name = name;
            this.totalComplaints = totalComplaints;
            this.resolvedComplaints = resolvedComplaints;
            this.pendingComplaints = pendingComplaints;
            this.employeeCount = employeeCount;
            this.budget = budget;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getTotalComplaints() {
            return totalComplaints;
        }

        public void setTotalComplaints(long totalComplaints) {
            this.totalComplaints = totalComplaints;
        }

        public long getResolvedComplaints() {
            return resolvedComplaints;
        }

        public void setResolvedComplaints(long resolvedComplaints) {
            this.resolvedComplaints = resolvedComplaints;
        }

        public long getPendingComplaints() {
            return pendingComplaints;
        }

        public void setPendingComplaints(long pendingComplaints) {
            this.pendingComplaints = pendingComplaints;
        }

        public long getEmployeeCount() {
            return employeeCount;
        }

        public void setEmployeeCount(long employeeCount) {
            this.employeeCount = employeeCount;
        }

        public double getBudget() {
            return budget;
        }

        public void setBudget(double budget) {
            this.budget = budget;
        }
    }

    public static class RecentActivityDto {
        private Long id;
        private String action;
        private String description;
        private String userName;
        private String timestamp;

        public RecentActivityDto() {
        }

        public RecentActivityDto(Long id, String action, String description, String userName, String timestamp) {
            this.id = id;
            this.action = action;
            this.description = description;
            this.userName = userName;
            this.timestamp = timestamp;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    // Getters and Setters
    public long getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }

    public long getOpenComplaints() {
        return openComplaints;
    }

    public void setOpenComplaints(long openComplaints) {
        this.openComplaints = openComplaints;
    }

    public long getInProgressComplaints() {
        return inProgressComplaints;
    }

    public void setInProgressComplaints(long inProgressComplaints) {
        this.inProgressComplaints = inProgressComplaints;
    }

    public long getResolvedComplaints() {
        return resolvedComplaints;
    }

    public void setResolvedComplaints(long resolvedComplaints) {
        this.resolvedComplaints = resolvedComplaints;
    }

    public long getOverdueComplaints() {
        return overdueComplaints;
    }

    public void setOverdueComplaints(long overdueComplaints) {
        this.overdueComplaints = overdueComplaints;
    }

    public long getEscalatedComplaints() {
        return escalatedComplaints;
    }

    public void setEscalatedComplaints(long escalatedComplaints) {
        this.escalatedComplaints = escalatedComplaints;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public long getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(long pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
    }

    public double getSlaComplianceRate() {
        return slaComplianceRate;
    }

    public void setSlaComplianceRate(double slaComplianceRate) {
        this.slaComplianceRate = slaComplianceRate;
    }

    public Map<String, Long> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(Map<String, Long> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public Map<String, Long> getPriorityDistribution() {
        return priorityDistribution;
    }

    public void setPriorityDistribution(Map<String, Long> priorityDistribution) {
        this.priorityDistribution = priorityDistribution;
    }

    public List<DepartmentStatDto> getDepartmentPerformance() {
        return departmentPerformance;
    }

    public void setDepartmentPerformance(List<DepartmentStatDto> departmentPerformance) {
        this.departmentPerformance = departmentPerformance;
    }

    public List<RecentActivityDto> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<RecentActivityDto> recentActivities) {
        this.recentActivities = recentActivities;
    }
}

