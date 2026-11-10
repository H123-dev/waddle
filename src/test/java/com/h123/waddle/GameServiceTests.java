package com.h123.waddle;


import com.h123.waddle.entities.*;
import com.h123.waddle.repos.*;
import com.h123.waddle.services.impls.GameServiceImpl;
import com.h123.waddle.services.interfaces.GameService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.InstanceOfAssertFactories.DURATION;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

public class GameServiceTests {
    @Mock
    private GuessRepo guessRepo;
    @Mock
    private MatchRepo matchRepo;
    @Mock
    private WordleRepo wordleRepo;
    @Mock
    private WordRepo wordRepo;
    @Mock
    private MatchRequestRepo matchRequestRepo;

    @InjectMocks
    private GameServiceImpl gameServiceImpl;

    private User u1;
    private User u2;
    private Match match;
    private Wordle w1;
    private Wordle w2;
    private List<Wordle> wordles;

    @BeforeEach
    public void generalVars() {
        u1 = new User("player1", "player1@gmail.com", "filler");
        u2 = new User("player2", "player2@gmail.com", "filler");
        match = new Match(new Word("VIPER"));
        ReflectionTestUtils.setField(match, "id", 1L);
        w1 = new Wordle(u1, match);
        w2 = new Wordle(u2, match);
        ReflectionTestUtils.setField(w1, "id", 1L);
        ReflectionTestUtils.setField(w2, "id", 2L);
        wordles = new ArrayList<>();
        wordles.add(w1);
        wordles.add(w2);
        when(wordleRepo.getWordles(Mockito.any(Long.class))).thenReturn(wordles);
    }

    @Test
    public void gameService_findWinner_1solved_ReturnsCorrectSolve()
    {
        //Araange
        Guess lg1 = new Guess("SLATE", w1);
        Guess lg2 = new Guess("VIPER", w2);

        when(guessRepo.getLastGuess(Mockito.eq(1L))).thenReturn(Optional.of(lg1));
        when(guessRepo.getLastGuess(Mockito.eq(2L))).thenReturn(Optional.of(lg2));

        //Act

        Wordle winner = gameServiceImpl.findWinner(match);

        //Asert
        assertThat(winner).isNotNull();
        assertThat(winner).isEqualTo(w2);
    }

    @Test
    public void gameService_findWinner_0solved_ReturnsTie()
    {
        //Araange
        Guess lg1 = new Guess("SLATE", w1);
        Guess lg2 = new Guess("PLATE", w2);

        when(guessRepo.getLastGuess(Mockito.eq(1L))).thenReturn(Optional.of(lg1));
        when(guessRepo.getLastGuess(Mockito.eq(2L))).thenReturn(Optional.of(lg2));

        //Act

        Wordle winner = gameServiceImpl.findWinner(match);

        //Asert
        assertThat(winner).isEqualTo(null);
    }

    @Test
    public void gameService_findWinner_2solved_ReturnsFewerGuesses()
    {
        //Araange
        ReflectionTestUtils.setField(w1,"progress",5);
        ReflectionTestUtils.setField(w2,"progress",3);
        Guess lg1 = new Guess("VIPER", w1);
        Guess lg2 = new Guess("VIPER", w2);

        when(guessRepo.getLastGuess(Mockito.eq(1L))).thenReturn(Optional.of(lg1));
        when(guessRepo.getLastGuess(Mockito.eq(2L))).thenReturn(Optional.of(lg2));

        //Act

        Wordle winner = gameServiceImpl.findWinner(match);

        //Asert
        assertThat(winner).isNotNull();
        assertThat(winner).isEqualTo(w2);
    }

    @Test
    public void gameService_findWinner_2solvedSameGuesses_ReturnsEarlierGuess()
    {
        //Araange
        ReflectionTestUtils.setField(w1,"progress",3);
        ReflectionTestUtils.setField(w2,"progress",3);
        ReflectionTestUtils.setField(w1,"finishTime", Instant.now());
        ReflectionTestUtils.setField(w2,"finishTime", Instant.now().minus(Duration.ofMinutes(2)));
        Guess lg1 = new Guess("VIPER", w1);
        Guess lg2 = new Guess("VIPER", w2);

        when(guessRepo.getLastGuess(Mockito.eq(1L))).thenReturn(Optional.of(lg1));
        when(guessRepo.getLastGuess(Mockito.eq(2L))).thenReturn(Optional.of(lg2));

        //Act

        Wordle winner = gameServiceImpl.findWinner(match);

        //Asert
        assertThat(winner).isNotNull();
        assertThat(winner).isEqualTo(w2);
    }

    @Test
    public void gameService_findWinner_2solvedSameGuessesSameTime_ReturnsTie()
    {
        //Araange
        Instant s = Instant.now();
        ReflectionTestUtils.setField(w1,"progress",3);
        ReflectionTestUtils.setField(w2,"progress",3);
        ReflectionTestUtils.setField(w1,"finishTime", s);
        ReflectionTestUtils.setField(w2,"finishTime", s);
        Guess lg1 = new Guess("VIPER", w1);
        Guess lg2 = new Guess("VIPER", w2);

        when(guessRepo.getLastGuess(Mockito.eq(1L))).thenReturn(Optional.of(lg1));
        when(guessRepo.getLastGuess(Mockito.eq(2L))).thenReturn(Optional.of(lg2));

        //Act

        Wordle winner = gameServiceImpl.findWinner(match);

        //Asert
        assertThat(winner).isEqualTo(null);
    }

}
