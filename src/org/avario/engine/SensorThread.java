package org.avario.engine;

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
	protected boolean retry = false;

	protected SensorThread(Activity activity) {
		this.activity = activity;
		thr = new Thread(this);
	}

	public void startSensor() {
		thr.start();
	}

	public void stop() {
		SensorManager sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.unregisterListener(this);
	}

	@Override
	public void run() {
		try {
			if (sensors != null) {
				do {
					SensorManager sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
					for (int sensor : sensors) {
						Logger.get().log("Try initializing sensor " + sensor);
						sensorManager.registerListener(this, sensorManager.getDefaultSensor(sensor), sensorSpeed);
					}
					Thread.sleep(1000);
				} while (retry);
			}
		} catch (Exception e) {
			Logger.get().log("Sensors initialization fail ", e);
		}
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
