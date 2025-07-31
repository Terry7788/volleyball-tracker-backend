package com.volleyball.scoretracker.repository;

import com.volleyball.scoretracker.model.GuestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestSessionRepository extends JpaRepository<GuestSession, Long> {
    Optional<GuestSession> findBySessionId(String sessionId);
    
    @Query("SELECT gs FROM GuestSession gs WHERE gs.expiresAt < :now")
    List<GuestSession> findExpiredSessions(LocalDateTime now);
}