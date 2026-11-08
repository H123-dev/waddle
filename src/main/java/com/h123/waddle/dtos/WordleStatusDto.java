package com.h123.waddle.dtos;

import com.h123.waddle.entities.enums.GameState;
import com.h123.waddle.entities.enums.Result;

import java.time.Instant;
import java.util.Optional;

public record WordleStatusDto(GameState state , int myProgress , int enemyProgress , Instant deadline , Optional<Result> result , Optional<String> word) {
}
