package com.smartcomplaint.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminResponseRequest {

    @NotBlank(message = "Response text cannot be empty")
    private String response;

    public AdminResponseRequest() {
    }

    public AdminResponseRequest(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
