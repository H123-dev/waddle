package com.h123.waddle.repos;

import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepo extends JpaRepository<Match,Long> {
}
