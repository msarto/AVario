package org.avario.engine.sensors;

import org.avario.engine.DataAccessObject;
import org.avario.engine.SensorProducer;
import org.avario.engine.SensorThread;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.filters.sensors.BaroSensorFilter;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class BaroSensorThread extends SensorThread<Float> {
	private BaroSensorFilter baroFilter = new BaroSensorFilter();
	private float prevPresure = 0;

	public BaroSensorThread(Activity activity) {
		super(activity);
		init();
	}

	protected void init() {
		sensors = new int[] { Sensor.TYPE_PRESSURE };
		sensorSpeed = SensorManager.SENSOR_DELAY_FASTEST;
		retry = true;
	}

	@Override
	public synchronized void notifySensorChanged(SensorEvent sensorEvent) {
		retry = false;
		float currentPresure = sensorEvent.values.clone()[0];
		final float diff = Math.abs(prevPresure - currentPresure);
		if (diff > ((50 - Preferences.baro_sensitivity) * 0.002f)) {
			// We will skip big consecutive differences to filter the big noise
			Logger.get().log("Skip " + diff);
			int filterSensitivity = (100 - Preferences.baro_sensitivity);
			currentPresure = prevPresure > 0 ? ((prevPresure * filterSensitivity + currentPresure
					* (100 - filterSensitivity)) / 100) : currentPresure;
		}
		final float altitude = baroFilter.toAltitude(currentPresure);
		if (altitude >= 0) {
			DataAccessObject.get().setBaroLastAltitude(altitude);
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					SensorProducer.get().notifyBaroConsumers(altitude);
				}
			});
		}
		prevPresure = currentPresure;
	}
}
