package org.avario.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class Logger {
	private static boolean USE_LOG = false;
	private static final String LOG_TAG = "AVARIO";
	private static Logger THIS = new Logger();
	private OutputStream logStream = null;

	public static void init() {
		if (!USE_LOG) {
			return;
		}
		try {
			THIS.close();
			File logFile = new File(Environment.getExternalStorageDirectory() + File.separator + "AVario"
					+ File.separator + "AVario.log");
			if (!logFile.getParentFile().exists()) {
				logFile.getParentFile().mkdirs();
			}
			THIS = new Logger();
			THIS.logStream = new BufferedOutputStream(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
		}
	}

	public static Logger get() {
		return THIS;
	}

	public void close() {
		if (!USE_LOG) {
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
		if (!USE_LOG) {
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

	public void log(String msg, Exception ex) {
		if (!USE_LOG) {
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
		} catch (IOException e) {
		} finally {
			Log.i(LOG_TAG, msg, ex);
		}
	}
}
