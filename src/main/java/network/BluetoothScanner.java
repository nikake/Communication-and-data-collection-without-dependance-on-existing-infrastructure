package main.java.network;

import main.java.util.CommandExecutor;
import main.java.util.Device;

public class BluetoothScanner implements Runnable {

    private CommandExecutor commandExecutor = new CommandExecutor();
    private static final String CMD = "python bluetooth_rssi.py";
    private Device device;

    public BluetoothScanner (Device device){
        this.device = device;
    }

    @Override
    public void run() {
        while(true) {
            String[] rssi = commandExecutor.execute((CMD + " " + device.btAddress).split("\\s+"));
            for (String s : rssi)
                System.out.println(s);
        }
    }
}
