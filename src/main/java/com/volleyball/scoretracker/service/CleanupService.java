package com.volleyball.scoretracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CleanupService {
    
    @Autowired
    private GuestSessionService guestSessionService;
    
    // Run every hour
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredGuestSessions() {
        guestSessionService.cleanupExpiredSessions();
    }
}