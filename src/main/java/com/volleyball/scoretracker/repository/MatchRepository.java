package com.volleyball.scoretracker.repository;

import com.volleyball.scoretracker.model.Match;
import com.volleyball.scoretracker.model.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    List<Match> findByStatus(MatchStatus status);
    
    @Query("SELECT m FROM Match m ORDER BY m.createdAt DESC")
    List<Match> findAllOrderByCreatedAtDesc();
    
    List<Match> findByTeam1NameContainingOrTeam2NameContaining(String team1Name, String team2Name);

    //Database Query
    @Query("SELECT m FROM Match m WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
    List<Match> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT m FROM Match m WHERE m.guestSession.sessionId = :sessionId ORDER BY m.createdAt DESC")
    List<Match> findByGuestSessionIdOrderByCreatedAtDesc(String sessionId);

    @Query("SELECT m FROM Match m WHERE m.user.id = :userId AND m.status = :status")
    List<Match> findByUserIdAndStatus(Long userId, MatchStatus status);

    @Query("SELECT m FROM Match m WHERE m.guestSession.sessionId = :sessionId AND m.status = :status")
    List<Match> findByGuestSessionIdAndStatus(String sessionId, MatchStatus status);
}