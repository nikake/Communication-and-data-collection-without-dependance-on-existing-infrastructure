package main.java.network;

import main.java.log.Logger;
import main.java.util.Device;
import main.java.util.MachineID;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SignalHandler {

    private final static String[] SCAN_COMMAND = {
            "/bin/sh",
            "-c",
            "iwlist wlan0 scan | egrep 'Address|ESSID|Quality|Signal'"
    };
    private final Device device;
    private final String iNetAddress;
    private Logger log;

    public SignalHandler(Device device, String iNetAddress) {
        this.device = device;
        this.iNetAddress = iNetAddress;
        log = Logger.getLogger(this.getClass().getSimpleName());
    }

    private String scan() {
        StringBuilder data = new StringBuilder();
        try {
            System.out.println("Executing commands: " + SCAN_COMMAND[2]);
            Process p = Runtime.getRuntime().exec(SCAN_COMMAND);
            p.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while((line = br.readLine().trim()) != null) {
                System.out.println("Appending: " + line);
                data.append(line + "\n");
            }
            while((line = error.readLine()) != null) {
                System.out.println("Error: " + line);
            }
            System.out.println("Scan complete. Length: " + data.length());
        } catch(Exception e) {
            System.out.println(e);
        }
        return data.toString();
    }

    public int getSignalStrength() {
        String[] data = scan().split("\n");
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
