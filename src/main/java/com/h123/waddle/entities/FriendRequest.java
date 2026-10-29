package com.h123.waddle.entities;

import com.h123.waddle.entities.enums.ReqState;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "friend_requests")
public class FriendRequest extends Request
{
    public FriendRequest(User sender, User receiver) {
        super(sender, receiver);
    }

    public FriendRequest() {
    }
}
