package main.java.log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import main.java.log.Logger.LogLineStorage;
import main.java.util.FileUtil;

public class LogWriter implements Runnable {

	private static File logFileStatic = new File("./Log/App.log");
	private static boolean append = true;

	private static class LogWriterHolder {
		private static final LogWriter INSTANCE = new LogWriter(logFileStatic, append);
	}

	public static LogWriter getInstance() {
		if (logFileStatic == null)
			return null;
		return LogWriterHolder.INSTANCE;
	}

	public static void setLogFile(File f) {
		logFileStatic = f;
	}

	public static void setAppend(boolean app) {
		append = app;
	}

	private File logFile;
	private boolean active;

	private LinkedBlockingQueue<LogLineStorage> writeBuffer = new LinkedBlockingQueue<LogLineStorage>();

	public LogWriter(File logFileLocation, boolean append) {
		logFile = logFileLocation;
	}

	@Override
	public void run() {
		active = true;
		if (!append) {
			if (logFile.exists()) {
				logFile.delete();
			}
		}
		while (active) {
			try {
				String toWrite = writeBuffer.take().toWrite();
				FileUtil.writeToFile(toWrite, logFile);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void add(LogLineStorage lls) {
		writeBuffer.add(lls);
	}
}