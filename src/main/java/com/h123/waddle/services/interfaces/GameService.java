package com.h123.waddle.services.interfaces;

import com.h123.waddle.dtos.GuessInDto;
import com.h123.waddle.dtos.GuessOutDto;

public interface GameService
{
    public GuessOutDto makeGuess(GuessInDto guessInDto , Long userID);
}
