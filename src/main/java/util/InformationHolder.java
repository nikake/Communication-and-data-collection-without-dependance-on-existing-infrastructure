package main.java.util;


import main.java.network.LocalClient;
import main.java.network.RemoteClient;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InformationHolder {

    private static CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Device> getDevices() {
        return devices;
    }

    public static void setDevices(Collection<Device> devices) {
        InformationHolder.devices.addAllAbsent(devices);
    }

    public static void removeDevice(Device device){
        InformationHolder.devices.remove(device);
    }

    public static ConcurrentHashMap<String, RemoteClient> remoteClients = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, LocalClient> localClients = new ConcurrentHashMap<>();
}


