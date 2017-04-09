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
        for(int i = 0; i < data.length - 3; i++) {
            System.out.println("[" + data[i] + "]");
            if(data[i].equals("ESSID:\"RPiAdHocNetwork\"")) {
                log.info(data[i]);
                log.info(data[i+1]);
                log.info(data[i+2]);
            }
        }
        return 0;
    }
}
