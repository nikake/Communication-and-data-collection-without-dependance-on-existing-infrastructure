package main.java.util;

import main.java.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandExecutor {

    private static Logger log = Logger.getLogger("CommandExecutor");

    public static String[] execute(String command) {
        return execute(new String[]{command});
    }

    public static String[] execute(String[] commands) {
        String data = "", errors = "";
        try {
            Process p = Runtime.getRuntime().exec(commands);
            p.waitFor();
            data = readData(p);
            errors = readErrors(p);
            if(!errors.isEmpty())
                log.error(errors);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return data.split("\n");
    }

    private static String readData(Process p) throws IOException {
        StringBuilder data = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while((line = br.readLine()) != null) {
            System.out.println("Adding line: " + line.trim());
            data.append(line.trim() + "\n");
        }
        return data.toString();
    }

    private static String readErrors(Process p) throws IOException {
        StringBuilder data = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        while((line = br.readLine()) != null) {
            data.append(line.trim() + "\n");
        }
        return data.toString();
    }
}
