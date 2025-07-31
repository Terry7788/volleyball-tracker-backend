package com.volleyball.scoretracker.repository;

import com.volleyball.scoretracker.model.SetScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetScoreRepository extends JpaRepository<SetScore, Long> {
    
    List<SetScore> findByMatchId(Long matchId);
    
    List<SetScore> findByMatchIdOrderBySetNumber(Long matchId);
}