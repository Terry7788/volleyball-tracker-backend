package com.volleyball.scoretracker.dto;

public class ValidationResponse {
    private boolean valid;
    
    // Default constructor
    public ValidationResponse() {}
    
    // Constructor with parameter
    public ValidationResponse(boolean valid) {
        this.valid = valid;
    }
    
    // Getters and setters
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
}