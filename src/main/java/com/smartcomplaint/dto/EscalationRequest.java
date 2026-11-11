package com.smartcomplaint.dto;

import jakarta.validation.constraints.NotBlank;

public class EscalationRequest {

    @NotBlank(message = "Escalation reason is required")
    private String reason;

    public EscalationRequest() {
    }

    public EscalationRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

