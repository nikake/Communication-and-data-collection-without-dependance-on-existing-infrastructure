package main.java.network;

import main.java.log.Logger;
import main.java.util.CommandExecutor;
import main.java.util.Device;

public class SignalHandler {

    private final static String[] SCAN_COMMAND = {
            "/bin/sh",
            "-c",
            "iwlist wlan0 scan | egrep 'Address|ESSID|Quality|Signal'"
    };
    private final Device device;
    private final String iNetAddress;
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    public SignalHandler(Device device, String iNetAddress) {
        this.device = device;
        this.iNetAddress = iNetAddress;

    }

    private String[] scan() {
        return CommandExecutor.execute(SCAN_COMMAND);
    }

    public int getSignalStrength() {
        String[] data = scan();
        String[] deviceData = new String[3];
        for(int i = 0; i < data.length - 3; i++) {
            System.out.println("[" + data[i] + "]");
            if(data[i].equals("ESSID:\"RPiAdHocNetwork\"")) {
                deviceData[0] = data[i];
                deviceData[1] = data[i-1];
                deviceData[2] = data[i+1];
            }
        }
        log.info("Signal strength to " + deviceData[1].split("Address: ")[1] + ": " + deviceData[2].split("Signal level=")[1]);
        return 0;
    }
}
