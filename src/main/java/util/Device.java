package main.java.util;

import java.net.Socket;

public class Device {

    private Socket deviceSocket = null;

    public Device(Socket deviceSocket) {
        this.deviceSocket = deviceSocket;
    }

    public Device() {

    }

    public String getIp(){
        return deviceSocket.getInetAddress().getHostAddress();
    }

}
