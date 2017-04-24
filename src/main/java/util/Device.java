package main.java.util;

public class Device {

    public final String ipAddress;
    public final String hwAddress;
    public final String btAddress;

    public Device(String ipAddress, String hwAddress, String btAddress) {
        this.ipAddress = ipAddress;
        this.hwAddress = hwAddress;
        this.btAddress = btAddress;
    }

}
