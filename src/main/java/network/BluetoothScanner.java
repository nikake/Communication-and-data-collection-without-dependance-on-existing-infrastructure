package main.java.network;

import main.java.util.CommandExecutor;
import main.java.util.Device;

public class BluetoothScanner implements Runnable {

    private static BluetoothScanner instance = new BluetoothScanner();
    private CommandExecutor commandExecutor = new CommandExecutor();
    private static final String CMD = "python bluetooth_rssi.py";

    public BluetoothScanner getInstance(){
        return instance;
    }

    @Override
    public void run() {

    }
}
