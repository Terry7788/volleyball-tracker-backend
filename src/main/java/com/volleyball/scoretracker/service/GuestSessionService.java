package com.volleyball.scoretracker.service;

import com.volleyball.scoretracker.model.GuestSession;
import com.volleyball.scoretracker.repository.GuestSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GuestSessionService {
    
    @Autowired
    private GuestSessionRepository guestSessionRepository;
    
    public GuestSession createGuestSession() {
        String sessionId = UUID.randomUUID().toString();
        GuestSession session = new GuestSession(sessionId);
        return guestSessionRepository.save(session);
    }
    
    public Optional<GuestSession> findBySessionId(String sessionId) {
        return guestSessionRepository.findBySessionId(sessionId);
    }
    
    public boolean isSessionValid(String sessionId) {
        Optional<GuestSession> session = findBySessionId(sessionId);
        return session.isPresent() && session.get().getExpiresAt().isAfter(LocalDateTime.now());
    }
    
    public void cleanupExpiredSessions() {
        List<GuestSession> expiredSessions = guestSessionRepository.findExpiredSessions(LocalDateTime.now());
        guestSessionRepository.deleteAll(expiredSessions);
    }
}