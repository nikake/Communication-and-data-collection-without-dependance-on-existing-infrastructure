package main.java.network;

import main.java.util.MachineID;

public class SignalHandler {

    private final MachineID device;
    private final String iNetAddress;

    public SignalHandler(MachineID device, String iNetAddress) {
        this.device = device;
        this.iNetAddress = iNetAddress;
    }
}
