package org.avario.engine.sensors;

import org.avario.engine.SensorProducer;
import org.avario.engine.SensorThread;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.filters.impl.IIRFilter;
import org.avario.utils.filters.sensors.CompasSensorFilter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class CompasSensorThread extends SensorThread<Float> {
	private CompasSensorFilter compasFilter = new CompasSensorFilter(Preferences.compass_filter_sensitivity);
	private SmoothCompassTask compassTask = new SmoothCompassTask();
	private IIRFilter accFilter = new IIRFilter(0.5f);
	protected long lastSensorTS = System.currentTimeMillis();

	public CompasSensorThread() {
		sensors = new int[] { Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ACCELEROMETER };
		sensorSpeed = SensorManager.SENSOR_DELAY_UI;
		new Thread(compassTask).start();
	}

	@Override
	public synchronized void notifySensorChanged(final SensorEvent sensorEvent) {
		if (System.currentTimeMillis() - lastSensorTS > 100) {
			lastSensorTS = System.currentTimeMillis();
			try {
				if (!isSensorProcessed) {
					isSensorProcessed = true;
					final float[] v = sensorEvent.values.clone();
					if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
						float bearing = compasFilter.toBearing(v);
						Logger.get().log("Bearing " + bearing);
						compassTask.setBearing(bearing);
					} else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
						float[] accelerometer = accFilter.doFilter(v);
						compasFilter.notifyAccelerometer(accelerometer);

						float x = accelerometer[0];
						float y = accelerometer[1];
						float z = accelerometer[2];
						SensorProducer.get().notifyAccelerometerConsumers(x, y, z);

						if (DataAccessObject.get() != null) {
							float gForce = x * x;
							gForce += y * y;
							gForce += z * z;
							gForce = (float) (Math.sqrt(gForce) - SensorManager.GRAVITY_EARTH);
							DataAccessObject.get().setGForce(Math.abs(gForce));
						}
					} else {
						Logger.get().log("Unknown compass sensor type " + sensorEvent.sensor.getType());
					}
				}
			} catch (Throwable t) {
				Logger.get().log("Error processing compass ", t);
			} finally {
				isSensorProcessed = false;
			}
		}

	}

	@Override
	public void stop() {
		super.stop();
	}

	private class SmoothCompassTask implements Runnable {
		private volatile float bearing = 0;
		private volatile Thread blinker;

		@Override
		public void run() {
			try {
				blinker = Thread.currentThread();
				while (blinker == Thread.currentThread()) {
					final float smoothBearing = compasFilter.smoothFilter(bearing);
					if (DataAccessObject.get() != null
							&& Math.abs(smoothBearing - bearing) > Preferences.compass_filter_sensitivity) {
						DataAccessObject.get().setBearing(smoothBearing);
						SensorProducer.get().notifyCompasConsumers(smoothBearing);
					}

					long wait = Math.round(80 - Math.abs(smoothBearing - bearing));
					Thread.sleep(wait > 50 ? wait : 50);
				}
			} catch (Exception ex) {
				Logger.get().log("Compass stopped...", ex);
			}
		}

		protected synchronized void setBearing(float bearing) {
			float bearingDiff = Math.abs(DataAccessObject.get().getBearing() - bearing);
			if (DataAccessObject.get().getBearing() != 0 && bearingDiff > 185) {
				bearing = 360 + bearing;
			}
			this.bearing = bearing;
		}
	}

}
