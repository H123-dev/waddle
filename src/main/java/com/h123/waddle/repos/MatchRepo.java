package com.h123.waddle.repos;

import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRepo extends JpaRepository<Match,Long> {

    @Query(value = "SELECT * FROM matches WHERE state = 'InProgress' AND dead_line <= NOW()", nativeQuery = true)
    List<Match> getALlFinished();
}
