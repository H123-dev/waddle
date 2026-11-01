package com.h123.waddle.repos;

import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRequestRepo extends JpaRepository<MatchRequest,Long> {
    @Query(value = "SELECT EXISTS (SELECT 1 FROM match_requests WHERE (sender_id = :senderId AND receiver_id = :receiverId) OR (sender_id = :receiverId AND receiver_id = :senderId))", nativeQuery = true)
    boolean isIn(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Query(value = "SELECT * FROM match_requests WHERE receiver_id = :id AND state = 'Pending'", nativeQuery = true)
    List<MatchRequest> getIncoming(@Param("id") Long id);

    @Query(value = "SELECT * FROM match_requests WHERE sender_id = :id AND state = 'Pending'", nativeQuery = true)
    List<MatchRequest> getOutgoing(@Param("id") Long id);
}

