package com.volleyball.scoretracker.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String team1Name;
    
    @Column(nullable = false)
    private String team2Name;
    
    @Column(nullable = false)
    private int team1Score = 0;  // Current set score
    
    @Column(nullable = false)
    private int team2Score = 0;  // Current set score
    
    @Column(nullable = false)
    private int team1Sets = 0;   // Sets won
    
    @Column(nullable = false)
    private int team2Sets = 0;   // Sets won
    
    @Column(nullable = false)
    private int currentSet = 1;
    
    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.IN_PROGRESS;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // NEW FIELDS FOR UNDO TRACKING
    @Column
    private String lastScoringTeam; // "team1" or "team2" - tracks who scored the last point
    
    @Column
    private LocalDateTime lastScoreTime; // When the last point was scored
    
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<SetScore> sets = new ArrayList<>();
    
    // User relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_session_id")
    @JsonBackReference
    private GuestSession guestSession;
    
    // Constructors
    public Match() {}
    
    public Match(String team1Name, String team2Name) {
        this.team1Name = team1Name;
        this.team2Name = team2Name;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public int getTeam1Score() {
        return team1Score;
    }
    
    public void setTeam1Score(int team1Score) {
        this.team1Score = team1Score;
    }
    
    public int getTeam2Score() {
        return team2Score;
    }
    
    public void setTeam2Score(int team2Score) {
        this.team2Score = team2Score;
    }
    
    public int getTeam1Sets() {
        return team1Sets;
    }
    
    public void setTeam1Sets(int team1Sets) {
        this.team1Sets = team1Sets;
    }
    
    public int getTeam2Sets() {
        return team2Sets;
    }
    
    public void setTeam2Sets(int team2Sets) {
        this.team2Sets = team2Sets;
    }
    
    public int getCurrentSet() {
        return currentSet;
    }
    
    public void setCurrentSet(int currentSet) {
        this.currentSet = currentSet;
    }
    
    public MatchStatus getStatus() {
        return status;
    }
    
    public void setStatus(MatchStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // NEW GETTERS AND SETTERS FOR UNDO TRACKING
    public String getLastScoringTeam() {
        return lastScoringTeam;
    }
    
    public void setLastScoringTeam(String lastScoringTeam) {
        this.lastScoringTeam = lastScoringTeam;
    }
    
    public LocalDateTime getLastScoreTime() {
        return lastScoreTime;
    }
    
    public void setLastScoreTime(LocalDateTime lastScoreTime) {
        this.lastScoreTime = lastScoreTime;
    }
    
    public List<SetScore> getSets() {
        return sets;
    }
    
    public void setSets(List<SetScore> sets) {
        this.sets = sets;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public GuestSession getGuestSession() {
        return guestSession;
    }
    
    public void setGuestSession(GuestSession guestSession) {
        this.guestSession = guestSession;
    }
}