package com.volleyball.scoretracker.controller;

import com.volleyball.scoretracker.dto.CreateMatchRequest;
import com.volleyball.scoretracker.dto.ScoreUpdateRequest;
import com.volleyball.scoretracker.model.Match;
import com.volleyball.scoretracker.model.MatchStatus;
import com.volleyball.scoretracker.repository.MatchRepository;
import com.volleyball.scoretracker.service.MatchService;
import com.volleyball.scoretracker.service.UserService;
import com.volleyball.scoretracker.security.JwtUtils;
import com.volleyball.scoretracker.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "http://localhost:3000")
public class MatchController {
    
    @Autowired
    private MatchRepository matchRepository;
    
    @Autowired
    private MatchService matchService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    // Helper method to extract user context from request
    private UserContext getUserContext(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String guestSessionId = request.getHeader("Guest-Session-Id");
        
        UserContext context = new UserContext();
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                if (jwtUtils.validateJwtToken(token)) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    Optional<User> user = userService.findByUsername(username);
                    if (user.isPresent()) {
                        context.userId = user.get().getId();
                        context.isAuthenticated = true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Invalid JWT token: " + e.getMessage());
            }
        } else if (guestSessionId != null && !guestSessionId.trim().isEmpty()) {
            context.guestSessionId = guestSessionId;
            context.isGuest = true;
        }
        
        return context;
    }
    
    // Get all matches for the authenticated user or guest session
    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches(HttpServletRequest request) {
        try {
            UserContext context = getUserContext(request);
            
            if (context.isAuthenticated) {
                List<Match> matches = matchService.getMatchesForUser(context.userId);
                return ResponseEntity.ok(matches);
            } else if (context.isGuest) {
                List<Match> matches = matchService.getMatchesForGuestSession(context.guestSessionId);
                return ResponseEntity.ok(matches);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            System.out.println("Error fetching matches: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get match by ID with ownership verification
    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatchById(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserContext context = getUserContext(request);
            
            if (!context.isAuthenticated && !context.isGuest) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Match match = matchService.getMatchById(id, context.userId, context.guestSessionId);
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            System.out.println("Error fetching match: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Create new match
    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody CreateMatchRequest request, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== MATCH CREATION DEBUG ===");
            System.out.println("Received request: " + request.getTeam1Name() + " vs " + request.getTeam2Name());
            
            // Debug headers first
            String authHeader = httpRequest.getHeader("Authorization");
            String guestSessionHeader = httpRequest.getHeader("Guest-Session-Id");
            System.out.println("Authorization header: " + authHeader);
            System.out.println("Guest-Session-Id header: " + guestSessionHeader);
            
            if (request.getTeam1Name() == null || request.getTeam2Name() == null ||
                request.getTeam1Name().trim().isEmpty() || request.getTeam2Name().trim().isEmpty()) {
                System.out.println("Bad request - empty team names");
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("Getting user context...");
            UserContext context = getUserContext(httpRequest);
            System.out.println("User context - isAuthenticated: " + context.isAuthenticated);
            System.out.println("User context - isGuest: " + context.isGuest);
            System.out.println("User context - userId: " + context.userId);
            System.out.println("User context - guestSessionId: " + context.guestSessionId);
            
            if (!context.isAuthenticated && !context.isGuest) {
                System.out.println("ERROR: No valid authentication context");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            System.out.println("Calling matchService.createMatch...");
            Match match = matchService.createMatch(
                request.getTeam1Name(), 
                request.getTeam2Name(), 
                context.userId, 
                context.guestSessionId
            );
            
            System.out.println("SUCCESS: Created match with ID: " + match.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(match);
        } catch (Exception e) {
            System.out.println("=== ERROR CREATING MATCH ===");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Full stack trace:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Update match score
    @PutMapping("/{id}/score")
    public ResponseEntity<Match> updateScore(@PathVariable Long id, @RequestBody ScoreUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            UserContext context = getUserContext(httpRequest);
            
            if (!context.isAuthenticated && !context.isGuest) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Match match = matchService.updateScore(id, request.getTeam(), context.userId, context.guestSessionId);
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            System.out.println("Error updating score: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Undo last point
    @PutMapping("/{id}/undo")
    public ResponseEntity<Match> undoLastPoint(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserContext context = getUserContext(request);
            
            if (!context.isAuthenticated && !context.isGuest) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Match match = matchService.undoLastPoint(id, context.userId, context.guestSessionId);
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            System.out.println("Error undoing point: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Reset current set
    @PutMapping("/{id}/reset-set")
    public ResponseEntity<Match> resetCurrentSet(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserContext context = getUserContext(request);
            
            if (!context.isAuthenticated && !context.isGuest) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Match match = matchService.resetCurrentSet(id, context.userId, context.guestSessionId);
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            System.out.println("Error resetting set: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get active (in-progress) matches
    @GetMapping("/active")
    public ResponseEntity<List<Match>> getActiveMatches(HttpServletRequest request) {
        try {
            UserContext context = getUserContext(request);
            
            if (context.isAuthenticated) {
                List<Match> matches = matchService.getActiveMatchesForUser(context.userId);
                return ResponseEntity.ok(matches);
            } else if (context.isGuest) {
                List<Match> matches = matchService.getActiveMatchesForGuestSession(context.guestSessionId);
                return ResponseEntity.ok(matches);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            System.out.println("Error fetching active matches: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Edit completed set
    @PutMapping("/{matchId}/sets/{setNumber}")
    public ResponseEntity<Match> editCompletedSet(
            @PathVariable Long matchId, 
            @PathVariable int setNumber,
            @RequestBody EditSetRequest request,
            HttpServletRequest httpRequest) {
        try {
            UserContext context = getUserContext(httpRequest);
            
            if (!context.isAuthenticated && !context.isGuest) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Match match = matchService.editCompletedSet(
                matchId, 
                setNumber, 
                request.getTeam1Points(), 
                request.getTeam2Points(),
                context.userId,
                context.guestSessionId
            );
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            System.out.println("Error editing set: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Delete a match
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserContext context = getUserContext(request);
            
            if (!context.isAuthenticated && !context.isGuest) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            matchService.deleteMatch(id, context.userId, context.guestSessionId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.out.println("Error deleting match: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Pause/Resume a match
    @PutMapping("/{id}/pause")
    public ResponseEntity<Match> pauseMatch(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserContext context = getUserContext(request);
            
            if (!context.isAuthenticated && !context.isGuest) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Match match = matchService.pauseMatch(id, context.userId, context.guestSessionId);
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            System.out.println("Error pausing/resuming match: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get match statistics
    @GetMapping("/statistics")
    public ResponseEntity<MatchService.MatchStatistics> getStatistics(HttpServletRequest request) {
        try {
            UserContext context = getUserContext(request);
            
            if (context.isAuthenticated) {
                MatchService.MatchStatistics stats = matchService.getMatchStatistics(context.userId);
                return ResponseEntity.ok(stats);
            } else if (context.isGuest) {
                MatchService.MatchStatistics stats = matchService.getMatchStatisticsForGuest(context.guestSessionId);
                return ResponseEntity.ok(stats);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            System.out.println("Error fetching statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Inner class for user context
    private static class UserContext {
        Long userId = null;
        String guestSessionId = null;
        boolean isAuthenticated = false;
        boolean isGuest = false;
    }
    
    // Inner class for edit set request (keeping existing structure)
    public static class EditSetRequest {
        private int team1Points;
        private int team2Points;
        
        public EditSetRequest() {}
        
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
    }
}