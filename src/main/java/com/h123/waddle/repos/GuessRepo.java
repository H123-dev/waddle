package com.h123.waddle.repos;

import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.Guess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuessRepo extends JpaRepository<Guess,Long> {
    @Query(value = "SELECT * FROM guesses WHERE game_id = :wordleID ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<Guess> getLastGuess(@Param("wordleID") Long wordleID);
}
