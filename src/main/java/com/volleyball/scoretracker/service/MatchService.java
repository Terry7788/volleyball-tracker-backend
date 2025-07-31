package com.volleyball.scoretracker.service;

import com.volleyball.scoretracker.model.Match;
import com.volleyball.scoretracker.model.MatchStatus;
import com.volleyball.scoretracker.model.SetScore;
import com.volleyball.scoretracker.model.User;
import com.volleyball.scoretracker.model.GuestSession;
import com.volleyball.scoretracker.repository.MatchRepository;
import com.volleyball.scoretracker.repository.SetScoreRepository;
import com.volleyball.scoretracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class MatchService {
    
    @Autowired
    private MatchRepository matchRepository;
    
    @Autowired
    private SetScoreRepository setScoreRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GuestSessionService guestSessionService;
    
    // Create a new match for registered user
    public Match createMatchForUser(String team1Name, String team2Name, Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        
        User user = optionalUser.get();
        Match match = new Match(team1Name.trim(), team2Name.trim());
        match.setUser(user);
        
        return matchRepository.save(match);
    }
    
    // Create a new match for guest user
    public Match createMatchForGuest(String team1Name, String team2Name, String sessionId) {
        Optional<GuestSession> optionalSession = guestSessionService.findBySessionId(sessionId);
        if (!optionalSession.isPresent()) {
            throw new RuntimeException("Invalid guest session");
        }
        
        if (!guestSessionService.isSessionValid(sessionId)) {
            throw new RuntimeException("Guest session has expired");
        }
        
        GuestSession session = optionalSession.get();
        Match match = new Match(team1Name.trim(), team2Name.trim());
        match.setGuestSession(session);
        
        return matchRepository.save(match);
    }
    
    // Generic create match method that determines user type
    public Match createMatch(String team1Name, String team2Name, Long userId, String guestSessionId) {
        if (userId != null) {
            return createMatchForUser(team1Name, team2Name, userId);
        } else if (guestSessionId != null) {
            return createMatchForGuest(team1Name, team2Name, guestSessionId);
        } else {
            throw new RuntimeException("Either user ID or guest session ID must be provided");
        }
    }
    
    // Get matches for registered user
    public List<Match> getMatchesForUser(Long userId) {
        return matchRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    // Get matches for guest session
    public List<Match> getMatchesForGuestSession(String sessionId) {
        if (!guestSessionService.isSessionValid(sessionId)) {
            throw new RuntimeException("Guest session has expired");
        }
        return matchRepository.findByGuestSessionIdOrderByCreatedAtDesc(sessionId);
    }
    
    // Get active matches for registered user
    public List<Match> getActiveMatchesForUser(Long userId) {
        return matchRepository.findByUserIdAndStatus(userId, MatchStatus.IN_PROGRESS);
    }
    
    // Get active matches for guest session
    public List<Match> getActiveMatchesForGuestSession(String sessionId) {
        if (!guestSessionService.isSessionValid(sessionId)) {
            throw new RuntimeException("Guest session has expired");
        }
        return matchRepository.findByGuestSessionIdAndStatus(sessionId, MatchStatus.IN_PROGRESS);
    }
    
    // Verify match ownership before operations
    private Match verifyMatchOwnership(Long matchId, Long userId, String guestSessionId) {
        Optional<Match> optionalMatch = matchRepository.findById(matchId);
        if (!optionalMatch.isPresent()) {
            throw new RuntimeException("Match not found");
        }
        
        Match match = optionalMatch.get();
        
        // Check if user owns this match
        if (userId != null) {
            if (match.getUser() == null || !match.getUser().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized: You don't own this match");
            }
        } 
        // Check if guest session owns this match
        else if (guestSessionId != null) {
            if (!guestSessionService.isSessionValid(guestSessionId)) {
                throw new RuntimeException("Guest session has expired");
            }
            if (match.getGuestSession() == null || 
                !match.getGuestSession().getSessionId().equals(guestSessionId)) {
                throw new RuntimeException("Unauthorized: You don't own this match");
            }
        } else {
            throw new RuntimeException("Authentication required");
        }
        
        return match;
    }
    
    // Updated edit current set score method - clears undo used flag
    public Match editCurrentSetScore(Long matchId, int team1Score, int team2Score, Long userId, String guestSessionId) {
    Match match = verifyMatchOwnership(matchId, userId, guestSessionId);
    
    if (match.getStatus() != MatchStatus.IN_PROGRESS) {
        throw new RuntimeException("Cannot edit score - match is not in progress");
    }
    
    // Validate scores
    if (team1Score < 0 || team2Score < 0) {
        throw new RuntimeException("Scores cannot be negative");
    }
    
    // Check if the scores would end the set immediately
    boolean isSetWon = false;
    if (match.getCurrentSet() == 5) {
        // Set 5: first to 15, win by 2
        isSetWon = (team1Score >= 15 && team1Score - team2Score >= 2) || 
                  (team2Score >= 15 && team2Score - team1Score >= 2);
    } else {
        // Sets 1-4: first to 25, win by 2
        isSetWon = (team1Score >= 25 && team1Score - team2Score >= 2) || 
                  (team2Score >= 25 && team2Score - team1Score >= 2);
    }
    
    // Update scores
    match.setTeam1Score(team1Score);
    match.setTeam2Score(team2Score);
    
    // Determine who likely scored last based on the higher score
    if (team1Score > team2Score) {
        match.setLastScoringTeam("team1");
    } else if (team2Score > team1Score) {
        match.setLastScoringTeam("team2");
    } else {
        match.setLastScoringTeam(null); // Tied, unclear who scored last
    }
    
    match.setLastScoreTime(LocalDateTime.now());
    
    // Clear undo used flag when scores are edited
    match.setUndoUsed(false);
    
    // If the edited scores would win the set, complete it
    if (isSetWon) {
        completeSet(match);
    }
    
    return matchRepository.save(match);
}
    // Edit a completed set
    public Match editCompletedSet(Long matchId, int setNumber, int team1Points, int team2Points, 
                                 Long userId, String guestSessionId) {
        Match match = verifyMatchOwnership(matchId, userId, guestSessionId);
        
        // Find the set to edit
        SetScore setToEdit = setScoreRepository.findByMatchIdOrderBySetNumber(matchId)
            .stream()
            .filter(set -> set.getSetNumber() == setNumber)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Set not found"));
        
        // Validate the new scores
        if (team1Points < 0 || team2Points < 0) {
            throw new RuntimeException("Scores cannot be negative");
        }
        
        // Check if the new scores would be valid for that set
        boolean isValidScore = false;
        if (setNumber == 5) {
            // Set 5: first to 15, win by 2
            isValidScore = (team1Points >= 15 && team1Points - team2Points >= 2) || 
                          (team2Points >= 15 && team2Points - team1Points >= 2);
        } else {
            // Sets 1-4: first to 25, win by 2
            isValidScore = (team1Points >= 25 && team1Points - team2Points >= 2) || 
                          (team2Points >= 25 && team2Points - team1Points >= 2);
        }
        
        if (!isValidScore) {
            throw new RuntimeException("Invalid score for set " + setNumber);
        }
        
        // Store old winner for comparison
        boolean oldTeam1Won = setToEdit.getTeam1Points() > setToEdit.getTeam2Points();
        boolean newTeam1Won = team1Points > team2Points;
        
        // Update the set score
        setToEdit.setTeam1Points(team1Points);
        setToEdit.setTeam2Points(team2Points);
        setScoreRepository.save(setToEdit);
        
        // Recalculate sets won if the winner changed
        if (oldTeam1Won != newTeam1Won) {
            recalculateMatchSets(match);
        }
        
        return matchRepository.save(match);
    }
    
    // Recalculate match sets based on completed sets
    private void recalculateMatchSets(Match match) {
        List<SetScore> completedSets = setScoreRepository.findByMatchIdOrderBySetNumber(match.getId());
        
        int team1Sets = 0;
        int team2Sets = 0;
        
        for (SetScore set : completedSets) {
            if (set.getTeam1Points() > set.getTeam2Points()) {
                team1Sets++;
            } else {
                team2Sets++;
            }
        }
        
        match.setTeam1Sets(team1Sets);
        match.setTeam2Sets(team2Sets);
        
        // Check if match should be completed or reopened
        if (team1Sets >= 3 || team2Sets >= 3) {
            match.setStatus(MatchStatus.COMPLETED);
        } else if (match.getStatus() == MatchStatus.COMPLETED) {
            // Reopen the match if it was completed but shouldn't be anymore
            match.setStatus(MatchStatus.IN_PROGRESS);
            match.setCurrentSet(completedSets.size() + 1);
            match.setTeam1Score(0);
            match.setTeam2Score(0);
        }
    }
    
    // Update score method - now clears undo used flag when new point is scored
    public Match updateScore(Long matchId, String team, Long userId, String guestSessionId) {
        Match match = verifyMatchOwnership(matchId, userId, guestSessionId);

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new RuntimeException("Match is not in progress");
        }

        // Update score and track who scored last
        if ("team1".equals(team)) {
            match.setTeam1Score(match.getTeam1Score() + 1);
            match.setLastScoringTeam("team1");
        } else if ("team2".equals(team)) {
            match.setTeam2Score(match.getTeam2Score() + 1);
            match.setLastScoringTeam("team2");
        } else {
            throw new RuntimeException("Invalid team");
        }

        // Update last score time
        match.setLastScoreTime(LocalDateTime.now());

        // IMPORTANT: Clear undo used flag when a new point is scored
        match.setUndoUsed(false);

        // Check if set is won
        if (isSetWon(match.getTeam1Score(), match.getTeam2Score(), match.getCurrentSet())) {
            completeSet(match);
        }

        return matchRepository.save(match);
    }

    // Check if a set is won (volleyball rules)
    private boolean isSetWon(int score1, int score2, int currentSet) {
        // Set 5 (deciding set): first to 15, must win by 2
        if (currentSet == 5) {
            if (score1 >= 15 && score1 - score2 >= 2) {
                return true;
            }
            if (score2 >= 15 && score2 - score1 >= 2) {
                return true;
            }
            return false;
        }
        
        // Sets 1-4: first to 25, must win by 2
        if (score1 >= 25 && score1 - score2 >= 2) {
            return true;
        }
        if (score2 >= 25 && score2 - score1 >= 2) {
            return true;
        }
        return false;
    }
    
    // Updated complete set method - clears undo used flag for new set
    private void completeSet(Match match) {
        // Save the completed set
        SetScore setScore = new SetScore(
                match.getCurrentSet(),
                match.getTeam1Score(),
                match.getTeam2Score(),
                match);
        setScoreRepository.save(setScore);

        // Determine set winner and update sets won
        if (match.getTeam1Score() > match.getTeam2Score()) {
            match.setTeam1Sets(match.getTeam1Sets() + 1);
        } else {
            match.setTeam2Sets(match.getTeam2Sets() + 1);
        }

        // DEBUG: Print current sets won
        System.out.println("Set " + match.getCurrentSet() + " completed!");
        System.out.println("Current sets won - Team1: " + match.getTeam1Sets() + ", Team2: " + match.getTeam2Sets());

        // Check if match is won (best of 5: first to 3 sets)
        if (match.getTeam1Sets() >= 3 || match.getTeam2Sets() >= 3) {
            match.setStatus(MatchStatus.COMPLETED);
            System.out.println(
                    "MATCH COMPLETED! Winner has " + Math.max(match.getTeam1Sets(), match.getTeam2Sets()) + " sets");
        } else {
            // Start next set
            match.setCurrentSet(match.getCurrentSet() + 1);
            match.setTeam1Score(0);
            match.setTeam2Score(0);
            // Clear tracking for new set
            match.setLastScoringTeam(null);
            match.setUndoUsed(false); // Clear undo used flag for new set
            System.out.println("Starting set " + match.getCurrentSet());
        }
    }

    // Updated undo method - now sets undo used flag
    public Match undoLastPoint(Long matchId, Long userId, String guestSessionId) {
    Match match = verifyMatchOwnership(matchId, userId, guestSessionId);
    
    if (match.getStatus() != MatchStatus.IN_PROGRESS) {
        throw new RuntimeException("Cannot undo - match is not in progress");
    }
    
    // Check if undo was already used
    if (Boolean.TRUE.equals(match.getUndoUsed())) {
        throw new RuntimeException("Undo already used. Score a point to enable undo again.");
    }
    
    // Check if there are any points to undo
    if (match.getTeam1Score() == 0 && match.getTeam2Score() == 0) {
        throw new RuntimeException("No points to undo");
    }
    
    // Simple logic: Only undo if we know who scored last
    if (match.getLastScoringTeam() == null) {
        throw new RuntimeException("Cannot undo - last scoring team unknown. Use 'Edit Score' instead.");
    }
    
    // Undo the last point from the team that scored it
    if ("team1".equals(match.getLastScoringTeam())) {
        if (match.getTeam1Score() > 0) {
            match.setTeam1Score(match.getTeam1Score() - 1);
        } else {
            throw new RuntimeException("Cannot undo - Team 1 has no points to remove");
        }
    } else if ("team2".equals(match.getLastScoringTeam())) {
        if (match.getTeam2Score() > 0) {
            match.setTeam2Score(match.getTeam2Score() - 1);
        } else {
            throw new RuntimeException("Cannot undo - Team 2 has no points to remove");
        }
    }

    // Mark that undo has been used
    match.setUndoUsed(true);

    // After undo, we don't know who scored before, so clear the tracking
    match.setLastScoringTeam(null);
    match.setLastScoreTime(LocalDateTime.now());

    return matchRepository.save(match);
}

// Updated reset current set method - clears undo used flag
public Match resetCurrentSet(Long matchId, Long userId, String guestSessionId) {
    Match match = verifyMatchOwnership(matchId, userId, guestSessionId);

    if (match.getStatus() != MatchStatus.IN_PROGRESS) {
        throw new RuntimeException("Cannot reset - match is not in progress");
    }

    match.setTeam1Score(0);
    match.setTeam2Score(0);
    match.setLastScoringTeam(null); // Clear last scoring team
    match.setLastScoreTime(LocalDateTime.now());

    // Clear undo used flag when set is reset
    match.setUndoUsed(false);
    
    return matchRepository.save(match);
}
    // Get match by ID with ownership verification
    public Match getMatchById(Long matchId, Long userId, String guestSessionId) {
        return verifyMatchOwnership(matchId, userId, guestSessionId);
    }
    
    // Delete a match (only if owned by user/session)
    public void deleteMatch(Long matchId, Long userId, String guestSessionId) {
        Match match = verifyMatchOwnership(matchId, userId, guestSessionId);
        
        // Delete associated set scores first
        List<SetScore> setScores = setScoreRepository.findByMatchId(matchId);
        setScoreRepository.deleteAll(setScores);
        
        // Delete the match
        matchRepository.delete(match);
    }
    
    // Pause/Resume match
    public Match pauseMatch(Long matchId, Long userId, String guestSessionId) {
        Match match = verifyMatchOwnership(matchId, userId, guestSessionId);
        
        if (match.getStatus() == MatchStatus.IN_PROGRESS) {
            match.setStatus(MatchStatus.PAUSED);
        } else if (match.getStatus() == MatchStatus.PAUSED) {
            match.setStatus(MatchStatus.IN_PROGRESS);
        } else {
            throw new RuntimeException("Cannot pause/resume completed match");
        }
        
        return matchRepository.save(match);
    }
    
    // Get match statistics for user
    public MatchStatistics getMatchStatistics(Long userId) {
        List<Match> userMatches = getMatchesForUser(userId);
        return calculateStatistics(userMatches);
    }
    
    // Get match statistics for guest session
    public MatchStatistics getMatchStatisticsForGuest(String sessionId) {
        List<Match> guestMatches = getMatchesForGuestSession(sessionId);
        return calculateStatistics(guestMatches);
    }
    
    // Helper method to calculate statistics
    private MatchStatistics calculateStatistics(List<Match> matches) {
        MatchStatistics stats = new MatchStatistics();
        
        for (Match match : matches) {
            stats.totalMatches++;
            
            if (match.getStatus() == MatchStatus.COMPLETED) {
                stats.completedMatches++;
                
                // Determine winner and update win/loss counts
                if (match.getTeam1Sets() > match.getTeam2Sets()) {
                    // Could track specific team wins if needed
                } else {
                    // Track losses
                }
            } else if (match.getStatus() == MatchStatus.IN_PROGRESS) {
                stats.activeMatches++;
            }
        }
        
        return stats;
    }
    
    // Inner class for statistics
    public static class MatchStatistics {
        private int totalMatches = 0;
        private int completedMatches = 0;
        private int activeMatches = 0;
        
        // Getters and setters
        public int getTotalMatches() { return totalMatches; }
        public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }
        
        public int getCompletedMatches() { return completedMatches; }
        public void setCompletedMatches(int completedMatches) { this.completedMatches = completedMatches; }
        
        public int getActiveMatches() { return activeMatches; }
        public void setActiveMatches(int activeMatches) { this.activeMatches = activeMatches; }
    }
}