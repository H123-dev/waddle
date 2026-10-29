package com.h123.waddle.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "guesses")
public class Guess
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String g;

    @ManyToOne
    private Wordle game;

    public Guess() {}

    public Guess(String g , Wordle game)
    {
        this.g = g;
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public String getGuess() {
        return g;
    }

    public Wordle getGame() {
        return game;
    }
}
