package com.volleyball.scoretracker.dto;

public class GuestSessionResponse {
    private String sessionId;
    private String expiresAt;
    
    // Default constructor
    public GuestSessionResponse() {}
    
    // Constructor with parameters
    public GuestSessionResponse(String sessionId, String expiresAt) {
        this.sessionId = sessionId;
        this.expiresAt = expiresAt;
    }
    
    // Getters and setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}