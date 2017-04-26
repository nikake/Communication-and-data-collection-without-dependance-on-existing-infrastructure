package main.java.util;

import java.io.Serializable;

public class Device implements Serializable {

    public final String ipAddress;
    public final String hwAddress;
    public final String btAddress;

    public Device(String ipAddress, String hwAddress, String btAddress) {
        this.ipAddress = ipAddress;
        this.hwAddress = hwAddress;
        this.btAddress = btAddress;
    }

    public String toString() {
        return "{ ipAddress: " + ipAddress + ", hwAddress: " + hwAddress + ", btAddress " + btAddress + "}";
    }

    public boolean equals(Object o){
        if(o instanceof Device)
            return hwAddress.equals(((Device) o).hwAddress);
        return false;
    }

}
