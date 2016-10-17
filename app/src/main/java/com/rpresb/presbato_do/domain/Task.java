package com.rpresb.presbato_do.domain;

public class Task {
    private Integer id;
    private String name;
    private State state;

    public Task(Integer id, String name, State state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    public Task(String name) {
        this.name = name;
        this.state = State.PENDING;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    public void changeToDone() {
        this.state = State.DONE;
    }

    public void changeToPending() {
        this.state = State.PENDING;
    }
}
