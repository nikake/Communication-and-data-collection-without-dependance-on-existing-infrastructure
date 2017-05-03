package main.java.network;

import main.java.util.CommandExecutor;
import main.java.util.Device;

import java.util.LinkedList;

public class BluetoothScanner implements Runnable {

    private static final int MAX_NUMBER_OF_VALUES = 10;
    private CommandExecutor commandExecutor = new CommandExecutor();
    private static final String CMD = "python bluetooth_rssi.py";
    public final Device device;
    private int rssiSum = 0;
    private LinkedList<Integer> rssiValues = new LinkedList<>();
    private int currentRssi = -100;
    private Object lock = new Object();

    public BluetoothScanner (Device device){
        this.device = device;
    }

    public int getRssi() {
        synchronized (lock) {
            return currentRssi;
        }
    }

    @Override
    public void run() {
        while(true) {
            String[] rssi = commandExecutor.execute((CMD + " " + device.btAddress).split("\\s+"));
            int rssiValue = -100;
            if(rssi.length > 0 && !rssi[0].isEmpty() && !rssi[0].equals("device not found"))
                rssiValue = Integer.valueOf(rssi[0]);
            addNewRssiValue(rssiValue);
            updateRssi();
        }
    }

    private void addNewRssiValue(int value) {
        synchronized (lock) {
            if (rssiValues.size() >= MAX_NUMBER_OF_VALUES)
                rssiSum -= rssiValues.removeFirst();
            rssiValues.addLast(value);
            rssiSum += value;
        }
    }

    private void updateRssi() {
        synchronized (lock) {
            currentRssi = rssiSum / rssiValues.size();
        }
    }
}
