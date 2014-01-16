package org.avario.engine.sensors;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.avario.utils.Logger;

import android.app.Activity;
import android.hardware.SensorEvent;
import android.os.Environment;

public class LogBaroThread extends BaroSensorThread {
	protected boolean logBaro = true;
	protected File logFile = new File(Environment.getExternalStorageDirectory() + File.separator + "AVario"
			+ File.separator + "baro.csv");
	private OutputStream logStream = null;
	private long prevTs = 0;

	public LogBaroThread(Activity activity) {
		try {
			logStream = new BufferedOutputStream(new FileOutputStream(logFile, false));
		} catch (FileNotFoundException e) {
			Logger.get().log("Fail to open baro log", e);
		}
	}

	@Override
	public synchronized void notifySensorChanged(SensorEvent sensorEvent) {
		logBaro(sensorEvent);
		super.notifySensorChanged(sensorEvent);
	}

	protected void logBaro(SensorEvent sensorEvent) {
		if (logStream != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(System.nanoTime() - prevTs).append(",");
			prevTs = System.nanoTime();
			sb.append(sensorEvent.values.clone()[0]).append("\r\n");
			try {
				logStream.write(sb.toString().getBytes());
			} catch (IOException e) {
				Logger.get().log("Fail to write" + sb.toString());
			}
		}
	}

	@Override
	public void stop() {
		if (logStream != null) {
			try {
				logStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
