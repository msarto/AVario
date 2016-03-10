package org.avario.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.avario.engine.prefs.Preferences;

import android.util.Log;

public class Logger {
	private static final String LOG_TAG = "AVARIO";
	private static Logger THIS = new Logger();
	private OutputStream logStream = null;

	public static void init() {
		if (!Preferences.enable_logs) {
			return;
		}
		THIS.close();

		File externalLogFolder = IOUtils.getStorageDirectory();
		File logFile = new File(externalLogFolder, "AVario.log");
		if (!logFile.getParentFile().exists()) {
			logFile.getParentFile().mkdirs();
		}
		THIS = new Logger(logFile);
	}

	public static Logger get() {
		return THIS;
	}

	protected Logger() {
		super();
	}

	protected Logger(File logFile) {
		super();
		try {
			logStream = new BufferedOutputStream(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}

	public void close() {
		if (!Preferences.enable_logs) {
			return;
		}

		if (logStream != null) {
			try {
				synchronized (LOG_TAG) {
					logStream.flush();
					logStream.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public void log(String msg) {
		if (!Preferences.enable_logs) {
			return;
		}

		try {
			if (logStream != null) {
				synchronized (LOG_TAG) {
					logStream.write((new Date().toString() + " - " + msg + "\r\n<br>").getBytes());
				}
			}
		} catch (IOException e) {
		} finally {
			Log.i(LOG_TAG, msg);
		}
	}

	public void log(String msg, Throwable ex) {
		if (!Preferences.enable_logs) {
			return;
		}

		try {
			if (logStream != null) {
				synchronized (LOG_TAG) {
					logStream.write((new Date().toString() + " - " + ex.getClass().getName() + " - " + msg + " - "
							+ ex.getMessage() + "\r\n<br>").getBytes());
					for (StackTraceElement stack : ex.getStackTrace()) {
						logStream.write((" \t" + stack.getClassName() + "#" + stack.getMethodName() + "#"
								+ stack.getLineNumber() + "\r\n<br>").getBytes());
					}
				}
			}
			Log.i(LOG_TAG, msg, ex);
		} catch (Throwable e) {
			System.err.print(msg);
		}
	}
}
