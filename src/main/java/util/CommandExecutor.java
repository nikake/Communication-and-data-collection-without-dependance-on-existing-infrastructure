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
            data = readData(new BufferedReader(new InputStreamReader(p.getInputStream())));
            errors = readErrors(new BufferedReader(new InputStreamReader(p.getErrorStream())));
            if(!errors.isEmpty())
                log.error(errors);
        } catch(Exception e) {
            System.out.println(e);
        }
        return data.split("\n");
    }

    private static String readData(BufferedReader br) throws IOException {
        StringBuilder data = new StringBuilder();
        String line;
        while((line = br.readLine().trim()) != null) {
            data.append(line + "\n");
        }
        return data.toString();
    }

    private static String readErrors(BufferedReader br) throws IOException {
        StringBuilder data = new StringBuilder();
        String line;
        while((line = br.readLine().trim()) != null) {
            data.append(line + "\n");
        }
        return data.toString();
    }
}
