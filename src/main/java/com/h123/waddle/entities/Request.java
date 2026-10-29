package com.h123.waddle.entities;

import com.h123.waddle.entities.enums.ReqState;
import jakarta.persistence.*;

@MappedSuperclass
public abstract class Request
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    private User sender;


    @ManyToOne
    private User receiver;

    @Enumerated(EnumType.STRING)
    private ReqState state;

    public Request(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.state = ReqState.Pending;
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

    public void setState(ReqState state) {
        this.state = state;
    }
}
