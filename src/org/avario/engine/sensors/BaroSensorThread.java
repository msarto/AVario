package org.avario.engine.sensors;

import org.avario.engine.SensorProducer;
import org.avario.engine.SensorThread;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.Filter;
import org.avario.utils.filters.impl.MedianFilter;
import org.avario.utils.filters.sensors.BaroSensorFilter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class BaroSensorThread extends SensorThread<Float> {
	private BaroSensorFilter baroFilter = new BaroSensorFilter();
	private Filter preFilterPresure = new MedianFilter(Preferences.baro_sensitivity * 18);

	public BaroSensorThread() {
		init();
	}

	protected void init() {
		sensors = new int[] { Sensor.TYPE_PRESSURE };
		sensorSpeed = SensorManager.SENSOR_DELAY_FASTEST;
		if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
			DataAccessObject.get().setMovementFactor(new LinearRegression());
		}
	}

	@Override
	public synchronized void notifySensorChanged(SensorEvent sensorEvent) {
		float currentPresure = sensorEvent.values.clone()[0];
		currentPresure = preFilterPresure.doFilter(currentPresure)[0];
		final float altitude = baroFilter.toAltitude(currentPresure);
		if (altitude >= 0) {
			DataAccessObject.get().setLastAltitude(altitude);
			DataAccessObject.get().getMovementFactor().notify(System.nanoTime() / 1000000d, altitude);
			callbackThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					SensorProducer.get().notifyBaroConsumers(altitude);
				}
			});
		}
	}
}
