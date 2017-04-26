package main.java.util;


import java.util.ArrayList;

public class InformationHolder {

    private static ArrayList<Device> devices;

    public static ArrayList<Device> getDevices() {
        return devices;
    }

    public static void setDevices(ArrayList<Device> devices) {
        InformationHolder.devices = devices;
    }
}


