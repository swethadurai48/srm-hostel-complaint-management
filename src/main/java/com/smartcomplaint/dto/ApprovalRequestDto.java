package com.smartcomplaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public class ApprovalRequestDto {

    private String approvalType = "WORK_ORDER";

    @PositiveOrZero(message = "Amount must be zero or positive")
    private Double amount = 0.0;

    @NotBlank(message = "Reason is required")
    private String reason;

    public ApprovalRequestDto() {
    }

    public ApprovalRequestDto(String approvalType, Double amount, String reason) {
        this.approvalType = approvalType;
        this.amount = amount;
        this.reason = reason;
    }

    public String getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(String approvalType) {
        this.approvalType = approvalType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

