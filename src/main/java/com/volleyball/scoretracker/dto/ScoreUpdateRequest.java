package com.volleyball.scoretracker.dto;

public class ScoreUpdateRequest {
    private String team;
    
    // Default constructor
    public ScoreUpdateRequest() {}
    
    // Constructor with parameter
    public ScoreUpdateRequest(String team) {
        this.team = team;
    }
    
    // Getters and setters
    public String getTeam() {
        return team;
    }
    
    public void setTeam(String team) {
        this.team = team;
    }
}