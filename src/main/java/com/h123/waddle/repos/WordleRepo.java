package com.h123.waddle.repos;

import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.Wordle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordleRepo extends JpaRepository<Wordle,Long> {

    @Query(value = "SELECT * FROM wordles WHERE match_id =:matchID AND id != :otherID", nativeQuery = true)
    Wordle getOther(@Param("matchID") Long matchID , @Param("otherID") Long otherID);

    @Query(value = "SELECT * FROM wordles WHERE match_id =:matchID", nativeQuery = true)
    List<Wordle> getWordles(@Param("matchID") Long matchID);


}
