package main.java.messaging;

import java.io.Serializable;

public enum Message implements Serializable {
    TOO_CLOSE,
    TOO_FAR_AWAY,
    SET_LEFT_NEIGHBOUR,
    SET_RIGHT_NEIGHTBOUR,
    OK,
    DENIED,
    GET_INFO,
    UPDATE_ROUTING_TABLE
}
