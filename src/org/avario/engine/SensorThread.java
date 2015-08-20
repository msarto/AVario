package org.avario.engine;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.avario.AVarioActivity;
import org.avario.utils.Logger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class SensorThread<T> implements Runnable, SensorEventListener {
	protected static ExecutorService callbackThreadPool = Executors.newCachedThreadPool();

	private final CountDownLatch semaphore = new CountDownLatch(1);
	protected int[] sensors;
	protected int sensorSpeed;
	protected SensorManager sensorManager;
	private boolean isSensorActive = false;
	protected volatile boolean isSensorProcessed = false;

	protected SensorThread() {
		this.sensorManager = (SensorManager) AVarioActivity.CONTEXT.getSystemService(Context.SENSOR_SERVICE);
	}

	public void startSensor() {
		callbackThreadPool.execute(this);
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
					if (isSensorActive) {
						semaphore.await(30, TimeUnit.SECONDS);
					}
					Logger.get().log(isSensorActive ? "DONE" : "NOT" + " Registered sensor " + sensorId);
				}
			}
		} catch (Exception e) {
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
			semaphore.countDown();
			notifySensorChanged(sensorEvent);
		} catch (Exception e) {
			Logger.get().log("Fail sensoring changed ", e);
		}

	}

	protected abstract void notifySensorChanged(SensorEvent sensorEvent);

}
