package com.smartcomplaint.dto;

import jakarta.validation.constraints.NotNull;

public class AssignmentRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String notes;

    public AssignmentRequest() {
    }

    public AssignmentRequest(Long employeeId, String notes) {
        this.employeeId = employeeId;
        this.notes = notes;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

