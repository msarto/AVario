package org.avario.engine;

import java.util.List;

import org.avario.AVarioActivity;
import org.avario.utils.Logger;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class SensorThread<T> implements Runnable, SensorEventListener {
	private Thread thr;
	protected final Activity activity;
	protected int[] sensors;
	protected int sensorSpeed;
	protected SensorManager sensorManager;
	private boolean isSensorActive = false;

	protected SensorThread(Activity activity) {
		this.activity = activity;
		this.sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		thr = new Thread(this);
	}

	public void startSensor() {
		thr.start();
	}

	public void stop() {
		if (isSensorActive) {
			sensorManager.unregisterListener(this);
		}
	}

	@Override
	public void run() {
		try {
			if (sensors != null) {
				for (int sensorId : sensors) {
					Logger.get().log("Try initializing sensor " + sensorId);
					isSensorActive = registerListener(sensorId, sensorSpeed);
					Logger.get().log(isSensorActive ? "DONE" : "NOT" + " Registered sensor " + sensorId);
				}
			}
		} catch (Exception e) {
			Logger.get().log("Sensors initialization fail ", e);
		}
	}

	private synchronized boolean registerListener(int sensorId, int speed) {
		List<Sensor> sensorsList = sensorManager.getSensorList(sensorId);
		return sensorsList.size() == 1 ? sensorManager.registerListener(this, sensorsList.get(0), sensorSpeed) : false;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO: do we care?

	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		// Notify all who have register as listeners with the
		// sensorEvent.values. The sensorEvent.values array sequence is
		// described for each sensor type here:
		// http://developer.android.com/reference/android/hardware/SensorEvent.html#values
		try {
			notifySensorChanged(sensorEvent);
		} catch (Exception e) {
			Logger.get().log("Fail sensoring changed ", e);
		}

	}

	protected abstract void notifySensorChanged(SensorEvent sensorEvent);

}
