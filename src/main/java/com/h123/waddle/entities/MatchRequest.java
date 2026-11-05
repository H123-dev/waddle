package com.h123.waddle.entities;

import com.h123.waddle.entities.enums.ReqState;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "match_requests")
public class MatchRequest extends Request
{
    @OneToOne
    private Match match;

    private Instant sentTime;

    public MatchRequest(User sender, User receiver)
    {
        super(sender, receiver);
        this.sentTime = Instant.now();
    }

    public MatchRequest(){}

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }
}
