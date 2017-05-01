package main.java.messaging;

import java.io.Serializable;

public enum Message implements Serializable {
    TOO_CLOSE,
    TOO_FAR_AWAY,
    SET_LEFT_NEIGHBOUR,
    SET_LEFT_NEIGHBOUR_OK,
    SET_LEFT_NEIGHBOUR_DENIED,
    SET_RIGHT_NEIGHBOUR,
    SET_RIGHT_NEIGHBOUR_OK,
    SET_RIGHT_NEIGHBOUR_DENIED,
    OK,
    DENIED,
    GET_INFO,
    GET_INFO_OK,
    UPDATE_ROUTING_TABLE
}
