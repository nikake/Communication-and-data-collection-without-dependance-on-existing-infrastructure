package main.java.messaging;

import main.java.network.RoutingTable;
import main.java.util.Device;

import java.io.Serializable;

public class DataPacket implements Serializable {

    public final Device SENDER;
    public final Device RECEIVER;
    public final Message MESSAGE;
    public final RoutingTable ROUTING_TABLE;

    public DataPacket(Device sender, Device receiver, Message msg, RoutingTable rTable) {
        this.SENDER= sender;
        this.RECEIVER = receiver;
        this.MESSAGE = msg;
        this.ROUTING_TABLE = rTable;
    }
}
