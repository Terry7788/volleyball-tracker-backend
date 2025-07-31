package com.volleyball.scoretracker.dto;

public class CreateMatchRequest {
    private String team1Name;
    private String team2Name;
    
    // Default constructor
    public CreateMatchRequest() {}
    
    // Constructor with parameters
    public CreateMatchRequest(String team1Name, String team2Name) {
        this.team1Name = team1Name;
        this.team2Name = team2Name;
    }
    
    // Getters and setters
    public String getTeam1Name() {
        return team1Name;
    }
    
    public void setTeam1Name(String team1Name) {
        this.team1Name = team1Name;
    }
    
    public String getTeam2Name() {
        return team2Name;
    }
    
    public void setTeam2Name(String team2Name) {
        this.team2Name = team2Name;
    }
}