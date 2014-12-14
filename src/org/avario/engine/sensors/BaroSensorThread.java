package org.avario.engine.sensors;

import org.avario.engine.SensorProducer;
import org.avario.engine.SensorThread;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.impl.Kalman2Filter;
import org.avario.utils.filters.impl.StabiloFilter;
import org.avario.utils.filters.sensors.BaroSensorFilter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class BaroSensorThread extends SensorThread<Float> {

	private static final Object baroLock = new Object();
	private volatile boolean bSensorOn = false;
	private BaroSensorFilter baroFilter = new BaroSensorFilter(
			new StabiloFilter(), new Kalman2Filter());

	public BaroSensorThread() {
		init();
	}

	protected void init() {
		sensors = new int[] { Sensor.TYPE_PRESSURE };
		sensorSpeed = Math.round(Preferences.baro_sensitivity * 3 / 50);
	}

	@Override
	public void notifySensorChanged(final SensorEvent sensorEvent) {
		if (!isSensorProcessed) {
			isSensorProcessed = true;
			try {
				synchronized (baroLock) {
					if (!bSensorOn) {
						bSensorOn = true;
						DataAccessObject.get().setMovementFactor(
								new LinearRegression());
					}
					float currentPresure = sensorEvent.values.clone()[0];
					final float altitude = baroFilter
							.toAltitude(currentPresure);
					if (altitude >= 0) {
						DataAccessObject.get().setLastAltitude(altitude);
						DataAccessObject.get().getMovementFactor()
								.notify(System.nanoTime() / 1000000d, altitude);
						SensorProducer.get().notifyBaroConsumers(altitude);
					}
				}
			} finally {
				isSensorProcessed = false;
			}
		}
	}
}
