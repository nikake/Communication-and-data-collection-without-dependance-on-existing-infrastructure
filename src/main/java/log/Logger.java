package main.java.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Logger {

	private LogWriter lw;
	private String context;

	private Logger(String context) {
		this.context = context;
		lw = LogWriter.getInstance();
	}

	public static Logger getLogger(String who) {
		return new Logger(who);
	}

	public void debug(String toLog) {
		LogLineStorage lls = new LogLineStorage(toLog, LogLevel.DEBUG, System.currentTimeMillis(), context);
		addToLog(lls);
	}

	public void error(String toLog) {
		LogLineStorage lls = new LogLineStorage(toLog, LogLevel.ERROR, System.currentTimeMillis(), context);
		addToLog(lls);
	}

	public void info(String toLog) {
		LogLineStorage lls = new LogLineStorage(toLog, LogLevel.INFO, System.currentTimeMillis(), context);
		addToLog(lls);
	}

	public void trace(String toLog) {
		LogLineStorage lls = new LogLineStorage(toLog, LogLevel.TRACE, System.currentTimeMillis(), context);
		addToLog(lls);
	}

	public void exception(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		LogLineStorage lls = new LogLineStorage(exceptionAsString, LogLevel.EXCEPTION, System.currentTimeMillis());
		addToLog(lls);
	}

	private void addToLog(LogLineStorage lls) {
		if (lw != null)
			lw.add(lls);
	}

	// private void writeToLog(String toLog,LogLevel lvl, long
	// currentTime,String who){
	// if(logFile == null)
	// return;
	//
	// Date time = new Date(currentTime);
	//
	// String output = "["+time+"] "+who+" "+lvl +" # "+toLog;
	//
	// try {
	// FileUtil.writeToFile(output, logFile);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	// Log helper classes

	private enum LogLevel {
		// Severity lowest down to highest
		ERROR, INFO, DEBUG, EXCEPTION, TRACE
	}

	public class LogLineStorage implements Comparable<LogLineStorage> {
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
