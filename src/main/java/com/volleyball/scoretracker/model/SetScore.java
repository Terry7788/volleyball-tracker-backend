package com.volleyball.scoretracker.model;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "set_scores")
public class SetScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private int setNumber;
    
    @Column(nullable = false)
    private int team1Points;
    
    @Column(nullable = false)
    private int team2Points;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @JsonBackReference
    private Match match;
    
    // Constructors
    public SetScore() {}
    
    public SetScore(int setNumber, int team1Points, int team2Points, Match match) {
        this.setNumber = setNumber;
        this.team1Points = team1Points;
        this.team2Points = team2Points;
        this.match = match;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public int getSetNumber() {
        return setNumber;
    }
    
    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }
    
    public int getTeam1Points() {
        return team1Points;
    }
    
    public void setTeam1Points(int team1Points) {
        this.team1Points = team1Points;
    }
    
    public int getTeam2Points() {
        return team2Points;
    }
    
    public void setTeam2Points(int team2Points) {
        this.team2Points = team2Points;
    }
    
    public Match getMatch() {
        return match;
    }
    
    public void setMatch(Match match) {
        this.match = match;
    }
}