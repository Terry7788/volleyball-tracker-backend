package com.volleyball.scoretracker.controller;

import com.volleyball.scoretracker.dto.GuestSessionResponse;
import com.volleyball.scoretracker.dto.ValidationResponse;
import com.volleyball.scoretracker.model.GuestSession;
import com.volleyball.scoretracker.service.GuestSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest")
@CrossOrigin(origins = "http://localhost:3000")
public class GuestController {
    
    @Autowired
    private GuestSessionService guestSessionService;
    
    @PostMapping("/session")
    public ResponseEntity<GuestSessionResponse> createGuestSession() {
        try {
            GuestSession session = guestSessionService.createGuestSession();
            
            GuestSessionResponse response = new GuestSessionResponse();
            response.setSessionId(session.getSessionId());
            response.setExpiresAt(session.getExpiresAt().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error creating guest session: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/session/{sessionId}/validate")
    public ResponseEntity<ValidationResponse> validateSession(@PathVariable String sessionId) {
        try {
            boolean isValid = guestSessionService.isSessionValid(sessionId);
            ValidationResponse response = new ValidationResponse();
            response.setValid(isValid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error validating session: " + e.getMessage());
            ValidationResponse response = new ValidationResponse();
            response.setValid(false);
            return ResponseEntity.ok(response);
        }
    }
    
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> deleteGuestSession(@PathVariable String sessionId) {
        try {
            // Optional: Add method to delete specific guest session
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("Error deleting guest session: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}