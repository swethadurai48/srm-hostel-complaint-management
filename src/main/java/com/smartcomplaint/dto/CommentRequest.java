package com.smartcomplaint.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {

    @NotBlank(message = "Comment message cannot be empty")
    private String message;

    private boolean isInternal = false;

    public CommentRequest() {
    }

    public CommentRequest(String message, boolean isInternal) {
        this.message = message;
        this.isInternal = isInternal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean internal) {
        isInternal = internal;
    }
}

