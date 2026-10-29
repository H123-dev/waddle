package com.h123.waddle.entities;

import com.h123.waddle.entities.enums.GameState;
import com.h123.waddle.entities.enums.Result;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "wordles")
public class Wordle
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User player;

    private int progress;

    @Enumerated(EnumType.STRING)
    private GameState state;

    private Instant finishTime;

    @ManyToOne
    private Match match;

    @Enumerated(EnumType.STRING)
    private Result result;

    public Wordle(){}

    public Wordle(User player , Match m)
    {
        this.player = player;
        this.progress = 0;
        this.state = GameState.InProgress;
        this.match = m;
    }

    public Long getId() {
        return id;
    }

    public User getPlayer() {
        return player;
    }

    public int getProgress() {
        return progress;
    }

    public GameState getState() {
        return state;
    }

    public Instant getFinishTime() {
        return finishTime;
    }

    public Match getMatch() {
        return match;
    }

    public Result getResult() {
        return result;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public void setFinishTime(Instant finishTime) {
        this.finishTime = finishTime;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
