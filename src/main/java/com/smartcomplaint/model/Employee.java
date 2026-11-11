package com.smartcomplaint.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Enterprise Employee Entity for Human Resource Management (HRM).
 */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 100)
    private String designation;

    @Column(length = 30, nullable = false)
    private String status = "ACTIVE"; // ACTIVE, ON_LEAVE, INACTIVE

    @Column(length = 255)
    private String skills;

    @Column(name = "assigned_tasks_count")
    private Integer assignedTasksCount = 0;

    @Column(name = "resolved_tasks_count")
    private Integer resolvedTasksCount = 0;

    @Column(name = "joined_date")
    private LocalDateTime joinedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Employee() {
    }

    public Employee(String employeeCode, String name, String email, Department department, String designation) {
        this.employeeCode = employeeCode;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.status = "ACTIVE";
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.joinedDate == null) {
            this.joinedDate = now;
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.assignedTasksCount == null) {
            this.assignedTasksCount = 0;
        }
        if (this.resolvedTasksCount == null) {
            this.resolvedTasksCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public Integer getAssignedTasksCount() {
        return assignedTasksCount;
    }

    public void setAssignedTasksCount(Integer assignedTasksCount) {
        this.assignedTasksCount = assignedTasksCount;
    }

    public Integer getResolvedTasksCount() {
        return resolvedTasksCount;
    }

    public void setResolvedTasksCount(Integer resolvedTasksCount) {
        this.resolvedTasksCount = resolvedTasksCount;
    }

    public LocalDateTime getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDateTime joinedDate) {
        this.joinedDate = joinedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

