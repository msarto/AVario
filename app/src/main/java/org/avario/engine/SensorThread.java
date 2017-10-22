package org.avario.engine;

import java.util.List;

import org.avario.AVarioActivity;
import org.avario.utils.Logger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class SensorThread<T> implements SensorEventListener {
	protected int[] sensors;
	protected int sensorSpeed;
	protected SensorManager sensorManager;
	private boolean isSensorActive = false;
	protected volatile boolean isSensorProcessed = false;

	protected SensorThread() {
		this.sensorManager = (SensorManager) AVarioActivity.CONTEXT.getSystemService(Context.SENSOR_SERVICE);
	}

	public void stop() {
		if (isSensorActive) {
			sensorManager.unregisterListener(this);
		}
	}

	public void startSensor() {
		try {
			for (int sensorId : sensors) {
				Logger.get().log("Try initializing sensor " + sensorId);
				isSensorActive = registerListener(sensorId, sensorSpeed);
				if (isSensorActive) {
					// semaphore.await(2, TimeUnit.SECONDS);
				}
				Logger.get().log((isSensorActive ? "DONE" : "NOT") + " Registered sensor " + sensorId);
			}
		} catch (Throwable e) {
			Logger.get().log("Sensors initialization fail ", e);
		}
	}

	private boolean registerListener(int sensorId, int speed) {
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
			// semaphore.countDown();
			notifySensorChanged(sensorEvent);
		} catch (Throwable e) {
			Logger.get().log("Fail sensoring changed ", e);
		}

	}

	protected abstract void notifySensorChanged(SensorEvent sensorEvent);

}
