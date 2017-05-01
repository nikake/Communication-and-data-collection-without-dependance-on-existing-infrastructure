package main.java.util;


import main.java.network.LocalClient;
import main.java.network.RemoteClient;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InformationHolder {

    private static CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<Device> getDevices() {
        return devices;
    }

    public static void setDevices(Collection<Device> devices) {
        InformationHolder.devices.addAll(devices);
    }

    public static ConcurrentHashMap<SocketAddress, RemoteClient> remoteClients = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<SocketAddress, LocalClient> localClients = new ConcurrentHashMap<>();
}


