package com.h123.waddle.services.impls;

import com.h123.waddle.dtos.GuessInDto;
import com.h123.waddle.dtos.GuessOutDto;
import com.h123.waddle.dtos.GuessTuple;
import com.h123.waddle.dtos.WordleStatusDto;
import com.h123.waddle.entities.*;
import com.h123.waddle.entities.enums.GameState;
import com.h123.waddle.entities.enums.LetterPlacement;
import com.h123.waddle.entities.enums.Result;
import com.h123.waddle.exceptions.BadEntryException;
import com.h123.waddle.exceptions.ForbiddenRequestException;
import com.h123.waddle.exceptions.ResourceConflictException;
import com.h123.waddle.exceptions.ResourceNotFoundException;
import com.h123.waddle.repos.*;
import com.h123.waddle.services.interfaces.GameService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class GameServiceImpl implements GameService {

    private final GuessRepo guessRepo;
    private final MatchRepo matchRepo;
    private final WordleRepo wordleRepo;
    private final WordRepo wordRepo;
    private final MatchRequestRepo matchRequestRepo;

    public GameServiceImpl(GuessRepo guessRepo, MatchRepo matchRepo, WordleRepo wordleRepo, WordRepo wordRepo, MatchRequestRepo matchRequestRepo) {
        this.guessRepo = guessRepo;
        this.matchRepo = matchRepo;
        this.wordleRepo = wordleRepo;
        this.wordRepo = wordRepo;
        this.matchRequestRepo = matchRequestRepo;
    }


    @Override
    public GuessOutDto makeGuess(GuessInDto guessInDto , Long userID) throws ResourceNotFoundException , ForbiddenRequestException , BadEntryException {
        if(!wordRepo.existsByWord(guessInDto.g()))
            throw new BadEntryException("not a real word");
        Optional<Wordle> maybe = wordleRepo.findById(guessInDto.waddleID());
        if(maybe.isEmpty())
            throw new ResourceNotFoundException("game not found");
        Wordle game = maybe.get();
        if(!game.getPlayer().getId().equals(userID))
            throw new ForbiddenRequestException("cant accses a game that isnt urs");
        if(game.getMatch().getDeadLine().isBefore(Instant.now())) {
            throw new ForbiddenRequestException("ran out of time !");
        }
        if(game.getState().equals(GameState.Finished))
            throw new ForbiddenRequestException("game already finished !");
        String realWord = game.getMatch().getWord().getWord();
        GuessTuple [] word = new GuessTuple[5];
        for(int i = 0 ; i<5;i++)
        {
            GuessTuple t ;
            char crr = guessInDto.g().charAt(i);
            int maybeIndex = realWord.indexOf(crr);
            if(maybeIndex==-1)
                t=new GuessTuple(crr, LetterPlacement.NON);
            else if(crr == realWord.charAt(i))
                t = new GuessTuple(crr, LetterPlacement.INPLACE);
            else
                t= new GuessTuple(crr, LetterPlacement.INWORD);
            word[i] = t;
        }
        game.setProgress(game.getProgress()+1);
        wordleRepo.save(game);
        Guess s = new Guess(guessInDto.g(),game);
        guessRepo.save(s);
        if(realWord.equals(guessInDto.g())||game.getProgress()==6)
        {
            game.setState(GameState.Finished);
            game.setFinishTime(Instant.now());
            wordleRepo.save(game);
            updateDeadline(game);
        }
        return new GuessOutDto(word,game.getProgress());
    }

    @Transactional
    @Override
    public Match createGame(MatchRequest matchRequest)
    {
        Word word = wordRepo.getRandomWord();
        Match match = new Match(word);
        Wordle w1 = new Wordle(matchRequest.getSender(),match);
        Wordle w2 = new Wordle(matchRequest.getReceiver(),match);
        match = matchRepo.save(match);
        wordleRepo.save(w1);
        wordleRepo.save(w2);
        return match;
    }

    @Override
    public WordleStatusDto getStatus(Long playerID, Long wordleID) throws ResourceNotFoundException , ForbiddenRequestException
    {
        Optional<Wordle> maybe = wordleRepo.findById(wordleID);
        if(maybe.isEmpty())
            throw new ResourceNotFoundException("wordle not found");
        Wordle w = maybe.get();
        if(!w.getPlayer().getId().equals(playerID))
            throw new ForbiddenRequestException("not ur game");
        Wordle other = wordleRepo.getOther(w.getMatch().getId(),wordleID);
        if(w.getMatch().getState().equals(GameState.Finished))
            return new WordleStatusDto(w.getMatch().getState(),w.getProgress(),other.getProgress(),w.getMatch().getDeadLine(),Optional.ofNullable(w.getResult()),Optional.of(w.getMatch().getWord().getWord()));
        return new WordleStatusDto(w.getMatch().getState(),w.getProgress(),other.getProgress(),w.getMatch().getDeadLine(),Optional.empty(),Optional.empty());
    }

    public void updateDeadline(Wordle wordle)
    {
        Wordle other = wordleRepo.getOther(wordle.getMatch().getId(),wordle.getId());
        Match match = wordle.getMatch();
        if(other.getState().equals(GameState.Finished))
        {
            Wordle winner = findWinner(match);
            if(winner==null)
            {
                wordleRepo.getWordles(match.getId()).forEach(
                        w -> {
                            w.setResult(Result.TIE);
                            wordleRepo.save(w);
                        }
                );
                return;
            }
            other = wordleRepo.getOther(match.getId(),winner.getId());
            winner.setResult(Result.WIN);
            other.setResult(Result.LOSE);
            wordleRepo.save(winner);
            wordleRepo.save(other);
            return;
        }
        Instant newDeadline = Instant.now().plus(Duration.ofMinutes(1));
        if(newDeadline.isBefore(match.getDeadLine()))
        {
            match.setDeadLine(newDeadline);
            matchRepo.save(match);
        }
    }


    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void matchHandler()
    {
        List<Match> matches = matchRepo.getALlFinished();
        matches.stream()
                .filter(match -> match.getState().equals(GameState.InProgress))
                .forEach(
                        match -> {
                            Wordle winner = findWinner(match);
                            List<Wordle> wordles = wordleRepo.getWordles(match.getId());
                            if(winner==null)
                            {
                                wordles.getLast().setResult(Result.TIE);
                                wordles.getFirst().setResult(Result.TIE);
                            }
                            else if(winner.getId().equals(wordles.getFirst().getId()))
                            {
                                wordles.getFirst().setResult(Result.WIN);
                                wordles.getLast().setResult(Result.LOSE);
                            }
                            else
                            {
                                wordles.getFirst().setResult(Result.LOSE);
                                wordles.getLast().setResult(Result.WIN);
                            }
                            wordleRepo.save(wordles.getFirst());
                            wordleRepo.save(wordles.getLast());
                        }
                );

    }

    public Wordle findWinner(Match match)
    {
        List<Wordle> wordles = wordleRepo.getWordles(match.getId());
        wordles.forEach(
                wordle ->
                {
                    if(wordle.getState().equals(GameState.InProgress)) {
                        wordle.setState(GameState.Finished);
                        wordleRepo.save(wordle);
                    }
                }
        );
        match.setState(GameState.Finished);
        matchRepo.save(match);
        matchRequestRepo.delAllDone();
        String realWord = match.getWord().getWord();
        Optional<Guess> maybe1 = guessRepo.getLastGuess(wordles.getFirst().getId());
        Optional<Guess> maybe2 = guessRepo.getLastGuess(wordles.getLast().getId());
        //check for if they even made a guess
        if(maybe1.isEmpty())
        {
            if(maybe2.isEmpty())
                return null;
            if(maybe2.get().getGuess().equals(realWord))
                return wordles.getLast();
            return null;
        }
        if(maybe2.isEmpty())
            if(maybe1.get().getGuess().equals(realWord))
                return wordles.getFirst();
            else
                return null;

        //check for if one got right
        String word1 = maybe1.get().getGuess();
        String word2 = maybe2.get().getGuess();
        if(word1.equals(realWord)&&!word2.equals(realWord))
            return wordles.getFirst();
        if(word2.equals(realWord)&&!word1.equals(realWord))
            return wordles.getLast();
        if(!word2.equals(realWord))
            return null;

        //check for if one got progress better
        int progress1 = wordles.getFirst().getProgress();
        int progress2 = wordles.getLast().getProgress();
        if(progress1>progress2)
            return wordles.getLast();
        if(progress2>progress1)
            return wordles.getFirst();

        //check for time

        Instant finish1 = wordles.getFirst().getFinishTime();
        Instant finish2 = wordles.getLast().getFinishTime();

        if(finish2.isAfter(finish1))
            return wordles.getFirst();
        if(finish1.isAfter(finish2))
            return wordles.getLast();

        //if all fails - tie

        return null;
    }

}
