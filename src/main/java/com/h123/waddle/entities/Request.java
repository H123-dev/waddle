package com.h123.waddle.entities;

import com.h123.waddle.entities.enums.ReqState;
import jakarta.persistence.*;

import java.time.Instant;

@MappedSuperclass
public abstract class Request
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected Instant answerTime;

    @ManyToOne
    protected User sender;


    @ManyToOne
    protected User receiver;

    @Enumerated(EnumType.STRING)
    protected ReqState state;

    public Request(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.state = ReqState.Pending;
        this.answerTime = null;
    }

    public Request() {
    }

    public Long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public ReqState getState() {
        return state;
    }

    public Instant getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(Instant answerTime) {
        this.answerTime = answerTime;
    }

    public void setState(ReqState state) {
        this.state = state;
    }
}
