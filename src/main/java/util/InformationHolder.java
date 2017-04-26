package main.java.util;


import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class InformationHolder {

    private static CopyOnWriteArrayList<Device> devices;

    public static CopyOnWriteArrayList<Device> getDevices() {
        return devices;
    }

    public static void setDevices(Collection<Device> devices) {
        InformationHolder.devices = (CopyOnWriteArrayList<Device>) devices;
    }
}


