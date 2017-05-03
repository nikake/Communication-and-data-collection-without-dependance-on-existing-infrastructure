package main.java.messaging;

public class MessageReader {

    public void readMessage(Message message) {
        switch (message) {
            case TOO_CLOSE:
                break;
            case TOO_FAR_AWAY:
                break;
            case SET_LEFT_NEIGHBOUR:
                break;
            case SET_RIGHT_NEIGHBOUR:
                break;
            case OK:
                break;
            case DENIED:
                break;
            case GET_INFO:
                break;
            case UPDATE_ROUTING_TABLE:
                break;
        }
    }
}
