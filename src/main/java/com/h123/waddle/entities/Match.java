package com.h123.waddle.entities;

import com.h123.waddle.entities.enums.GameState;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "matches")
public class Match
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Word word;

    private Instant startTime;

    private Instant deadLine;

    @Enumerated(EnumType.STRING)
    private GameState state;

    public Match(){}

    public Match(Word word)
    {
        this.word =word;
        this.startTime = Instant.now();
        this.deadLine = startTime.plus(Duration.ofMinutes(6));
        this.state = GameState.InProgress;
    }

    public Long getId() {
        return id;
    }

    public Word getWord() {
        return word;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getDeadLine() {
        return deadLine;
    }

    public GameState getState() {
        return state;
    }

    public void setDeadLine(Instant deadLine) {
        this.deadLine = deadLine;
    }

    public void setState(GameState state) {
        this.state = state;
    }
}
