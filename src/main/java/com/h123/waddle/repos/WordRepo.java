package com.h123.waddle.repos;

import com.h123.waddle.entities.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WordRepo extends JpaRepository<Word,Long> {


    @Query(value = "SELECT * FROM words ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Word getRandomWord();

    Optional<Word> findByWord(String word);
}
