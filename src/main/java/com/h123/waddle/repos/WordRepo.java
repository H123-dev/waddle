package com.h123.waddle.repos;

import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.MatchRequest;
import com.h123.waddle.entities.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordRepo extends JpaRepository<Word,Long> {


    @Query(value = "SELECT * FROM words ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Word getRandomWord();
}
