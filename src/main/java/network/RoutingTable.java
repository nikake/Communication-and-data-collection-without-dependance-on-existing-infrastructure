package main.java.network;

import main.java.util.Device;

import java.util.HashMap;

public class RoutingTable {

    private static HashMap<Device, Device[]> table = new HashMap<>();

    public static void add(Device device, Device[] neighbours){
        table.put(device, neighbours);
    }

    public static void remove(Device device){
        table.remove(device);
    }

    public static void replace(HashMap<Device, Device[]> newTable){
        table = newTable;
    }

    public static HashMap<Device, Device[]> getTable(){
        return table;
    }

    public static boolean contains(Device device){
        return table.containsKey(device);
    }

}
