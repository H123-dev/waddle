package com.h123.waddle.repos;

import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.Wordle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordleRepo extends JpaRepository<Wordle,Long> {

}
