package com.smartcomplaint.dto;

import com.smartcomplaint.model.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ComplaintStatus status;

    private String assignedStaff;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(ComplaintStatus status) {
        this.status = status;
    }

    public StatusUpdateRequest(ComplaintStatus status, String assignedStaff) {
        this.status = status;
        this.assignedStaff = assignedStaff;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(String assignedStaff) {
        this.assignedStaff = assignedStaff;
    }
}
