package com.rpresb.presbato_do.domain;

public enum State {

    PENDING(0),
    DONE(1),
    DELETED(2);

    private int type;

    State(int i) {
        this.type = i;
    }

    public int getNumericType() {
        return type;
    }

    public static State fromInteger(int x) {
        switch(x) {
            case 0:
                return PENDING;
            case 1:
                return DONE;
            case 2:
                return DELETED;
        }
        return null;
    }}
