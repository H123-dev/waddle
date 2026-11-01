package com.h123.waddle.repos;

import com.h123.waddle.entities.FriendRequest;
import com.h123.waddle.entities.Request;
import com.h123.waddle.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepo  extends JpaRepository<FriendRequest,Long> {

    @Query(value = "SELECT EXISTS (SELECT 1 FROM friend_requests WHERE (sender_id = :senderId AND receiver_id = :receiverId) OR (sender_id = :receiverId AND receiver_id = :senderId))", nativeQuery = true)
    boolean isIn(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Query(value = "SELECT * FROM friend_requests WHERE (sender_id =:id OR receiver_id = :id) AND state = 'Accepted'", nativeQuery = true)
    List<FriendRequest> getFriends(@Param("id") Long id);

    @Query(value = "SELECT * FROM friend_requests WHERE receiver_id = :id AND state = 'Pending'", nativeQuery = true)
    List<FriendRequest> getIncoming(@Param("id") Long id);

    @Query(value = "SELECT * FROM friend_requests WHERE sender_id = :id AND state = 'Pending'", nativeQuery = true)
    List<FriendRequest> getOutgoing(@Param("id") Long id);


}
