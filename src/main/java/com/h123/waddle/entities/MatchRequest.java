package com.h123.waddle.entities;

import com.h123.waddle.entities.enums.ReqState;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "match_requests")
public class MatchRequest extends Request
{
    @OneToOne
    private Match match;

    public MatchRequest(User sender, User receiver)
    {
        super(sender, receiver);
    }

    public MatchRequest(){}

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }
}
