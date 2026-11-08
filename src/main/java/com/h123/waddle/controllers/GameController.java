package com.h123.waddle.controllers;

import com.h123.waddle.dtos.GuessInDto;
import com.h123.waddle.dtos.GuessOutDto;
import com.h123.waddle.dtos.WordleStatusDto;
import com.h123.waddle.services.interfaces.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/waddle/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    //enter guess
    @PostMapping("/guess")
    public ResponseEntity<GuessOutDto> makGuess(@AuthenticationPrincipal Long userId , @RequestBody @Valid GuessInDto guessInDto)
    {
        GuessOutDto out = gameService.makeGuess(guessInDto,userId);
        return new ResponseEntity<>(out, HttpStatus.CREATED);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<WordleStatusDto> getStatus(@AuthenticationPrincipal Long userId , @PathVariable("id") Long wordleID)
    {
        WordleStatusDto out = gameService.getStatus(userId,wordleID);
        return new ResponseEntity<>(out,HttpStatus.OK);
    }
}
