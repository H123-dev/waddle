package com.h123.waddle.services.impls;

import com.h123.waddle.dtos.GuessInDto;
import com.h123.waddle.dtos.GuessOutDto;
import com.h123.waddle.dtos.GuessTuple;
import com.h123.waddle.entities.Guess;
import com.h123.waddle.entities.User;
import com.h123.waddle.entities.Wordle;
import com.h123.waddle.entities.enums.GameState;
import com.h123.waddle.entities.enums.LetterPlacement;
import com.h123.waddle.exceptions.ForbiddenRequestException;
import com.h123.waddle.exceptions.ResourceNotFoundException;
import com.h123.waddle.repos.GuessRepo;
import com.h123.waddle.repos.MatchRepo;
import com.h123.waddle.repos.UserRepo;
import com.h123.waddle.repos.WordleRepo;
import com.h123.waddle.services.interfaces.GameService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class GameServiceImpl implements GameService {

    private final GuessRepo guessRepo;
    private final MatchRepo matchRepo;
    private final WordleRepo wordleRepo;

    public GameServiceImpl(GuessRepo guessRepo, MatchRepo matchRepo, WordleRepo wordleRepo) {
        this.guessRepo = guessRepo;
        this.matchRepo = matchRepo;
        this.wordleRepo = wordleRepo;
    }


    @Override
    public GuessOutDto makeGuess(GuessInDto guessInDto , Long userID) throws ResourceNotFoundException , ForbiddenRequestException {
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
        if(realWord.equals(guessInDto.g())||game.getProgress()==5)
        {
            game.setState(GameState.Finished);
            game.setFinishTime(Instant.now());
            matchHandler();
        }
        game.setProgress(game.getProgress()+1);
        wordleRepo.save(game);
        Guess s = new Guess(guessInDto.g(),game);
        guessRepo.save(s);
        return new GuessOutDto(word,game.getProgress());
    }

    public void matchHandler()
    {
        //handles wordle vs wordle
    }
}
