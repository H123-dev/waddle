package com.h123.waddle.services.interfaces;

import com.h123.waddle.dtos.GuessInDto;
import com.h123.waddle.dtos.GuessOutDto;
import com.h123.waddle.entities.Match;
import com.h123.waddle.entities.MatchRequest;

public interface GameService
{
    public GuessOutDto makeGuess(GuessInDto guessInDto , Long userID);
    public Match createGame(MatchRequest matchRequest);
}
