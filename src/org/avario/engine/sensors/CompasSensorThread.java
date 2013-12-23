package org.avario.engine.sensors;

import org.avario.engine.SensorProducer;
import org.avario.engine.SensorThread;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.filters.sensors.CompasSensorFilter;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class CompasSensorThread extends SensorThread<Float> {
	private CompasSensorFilter compasFilter = new CompasSensorFilter(Preferences.compass_filter_sensitivity);
	private SmoothCompassTask compassTask = new SmoothCompassTask();

	public CompasSensorThread(Activity activity) {
		super(activity);
		sensors = new int[] { Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ACCELEROMETER };
		sensorSpeed = SensorManager.SENSOR_DELAY_UI;
		compassTask.startCompasss();
	}

	@Override
	public synchronized void notifySensorChanged(SensorEvent sensorEvent) {
		if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			float bearing = compasFilter.toBearing(sensorEvent.values.clone());
			compassTask.setBearing(bearing);
		} else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			compasFilter.notifyAccelerometer(sensorEvent.values.clone());
		}
	}

	@Override
	public void stop() {
		super.stop();
		compassTask.stop();
	}

	private class SmoothCompassTask implements Runnable {
		private Thread thr;
		private volatile float bearing = 0;
		private volatile Thread blinker;

		private SmoothCompassTask() {
			thr = new Thread(this);
		}

		public void startCompasss() {
			thr.start();
		}

		public void stop() {
			blinker = null;
			thr.interrupt();
		}

		@Override
		public void run() {
			try {
				blinker = Thread.currentThread();
				while (blinker == Thread.currentThread()) {
					final float smoothBearing = compasFilter.smoothFilter(bearing);
					if (DataAccessObject.get() != null
							&& Math.abs(smoothBearing - bearing) > Preferences.compass_filter_sensitivity) {

						DataAccessObject.get().setBearing(smoothBearing);
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								SensorProducer.get().notifyCompasConsumers(smoothBearing);
							}
						});
					}

					long wait = Math.round(80 - Math.abs(smoothBearing - bearing));
					Thread.sleep(wait > 5 ? wait : 5);
				}
			} catch (Exception ex) {
				Logger.get().log("Compass stopped...", ex);
			}
		}

		protected void setBearing(float bearing) {
			float bearingDiff = Math.abs(DataAccessObject.get().getBearing() - bearing);
			if (DataAccessObject.get().getBearing() != 0 && bearingDiff > 185) {
				bearing = 360 + bearing;
			}
			this.bearing = bearing;
		}
	}

}
