package com.h123.waddle.repos;

import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.Guess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuessRepo extends JpaRepository<Guess,Long> {
}
