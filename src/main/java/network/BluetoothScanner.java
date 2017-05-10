package main.java.network;

import main.java.util.CommandExecutor;
import main.java.util.Device;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BluetoothScanner implements Runnable {

    private static final String RSSI_CMD = "python bluetooth_rssi.py";
    private static final int MAX_WORKERS = 4;
    private static final int MAX_NUMBER_OF_VALUES = 10;

    private final Executor WORKER_POOL = Executors.newFixedThreadPool(MAX_WORKERS);

    private LinkedList<Integer> rssiValues = new LinkedList<>();
    private CommandExecutor commandExecutor = new CommandExecutor();
    private int rssiSum = 0;
    private int currentRssi = -100;
    private boolean keepRunning = true;

    private Object lock = new Object();

    public final Device device;

    public BluetoothScanner (Device device){
        this.device = device;
    }

    public int getRssi() {
        synchronized (lock) {
            return currentRssi;
        }
    }

    public void close() {
        keepRunning = false;
    }

    @Override
    public void run() {
        for(int i = 0; i < MAX_WORKERS; i++)
            WORKER_POOL.execute(new ScannerScript());
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

    private class ScannerScript implements Runnable {

        @Override
        public void run() {
            while(keepRunning) {
                String[] rssi = commandExecutor.execute((RSSI_CMD + " " + device.btAddress).split("\\s+"));
                int rssiValue = -300;
                if(rssi.length > 0 && !rssi[0].isEmpty() && !rssi[0].equals("device not found"))
                    rssiValue = Integer.valueOf(rssi[0]);
                addNewRssiValue(rssiValue);
                updateRssi();
            }
        }
    }
}
