package com.h123.waddle.repos;

import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepo  extends JpaRepository<FriendRequest,Long> {

    @Query(value = "SELECT EXISTS (SELECT 1 FROM friend_requests WHERE (sender_id = :senderId AND receiver_id = :receiverId) OR (sender_id = :receiverId AND receiver_id = :senderId))", nativeQuery = true)
    boolean isIn(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);


}
