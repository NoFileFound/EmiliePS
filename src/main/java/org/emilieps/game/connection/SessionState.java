package org.emilieps.game.connection;

public enum SessionState {
    CLOSED,
    WAITING_FOR_TOKEN,
    WAITING_FOR_LOGIN,
    WAITING_FOR_PICKING_CHARACTER,
    ACTIVE
}