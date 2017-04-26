package main.java.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Logger {

	private static LogWriter lw = LogWriter.getInstance();
	private static String context;

	private static class LoggerHolder {
		public static Logger logger = new Logger("Logger");
	}

	private Logger(String context) {
		this.context = context;
		System.out.println("Logger initiated");
		new Thread(lw).start();
	}

	public static Logger getLogger() {
		return LoggerHolder.logger;
	}

	public static void debug(String toLog) {
		LogLineStorage lls = new LogLineStorage(toLog, LogLevel.DEBUG, System.currentTimeMillis(), context);
		addToLog(lls);
	}

	public static void error(String toLog) {
		LogLineStorage lls = new LogLineStorage(toLog, LogLevel.ERROR, System.currentTimeMillis(), context);
		addToLog(lls);
	}

	public static void info(String toLog) {
		LogLineStorage lls = new LogLineStorage(toLog, LogLevel.INFO, System.currentTimeMillis(), context);
		addToLog(lls);
	}

	public static void trace(String toLog) {
		LogLineStorage lls = new LogLineStorage(toLog, LogLevel.TRACE, System.currentTimeMillis(), context);
		addToLog(lls);
	}

	public static void exception(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		LogLineStorage lls = new LogLineStorage(exceptionAsString, LogLevel.EXCEPTION, System.currentTimeMillis());
		addToLog(lls);
	}

	private static void addToLog(LogLineStorage lls) {
		if (lw != null)
			lw.add(lls);
	}

	private static enum LogLevel {
		// Severity lowest down to highest
		ERROR, INFO, DEBUG, EXCEPTION, TRACE
	}

	public static class LogLineStorage implements Comparable<LogLineStorage> {
		private String toLog;
		private LogLevel lvl;
		private long time;
		private String who;

		public LogLineStorage(String toLog, LogLevel lvl, long time, String who) {
			this.toLog = toLog;
			this.lvl = lvl;
			this.time = time;
			this.who = who;
		}

		public LogLineStorage(String exceptionAsString, LogLevel stacktrace, long time) {
			this.toLog = exceptionAsString;
			this.lvl = stacktrace;
			this.time = time;
		}

		public String toWrite() {
			DateFormat df = new SimpleDateFormat("y-M-d HH:mm:ss.SSS");
			String timeOutput = df.format(time);
			return "[" + timeOutput + "] " + lvl + "\t" + who + " :: " + toLog;
		}

		@Override
		public int compareTo(LogLineStorage lls) {
			long timeDiff = this.time = lls.time;
			if (timeDiff == 0)
				return 0;
			if (timeDiff > 0)
				return 1;
			return -1;
		}
	}

}
