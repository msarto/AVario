package org.avario.engine.sensors;

import org.avario.engine.DataAccessObject;
import org.avario.engine.SensorProducer;
import org.avario.engine.SensorThread;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.Filter;
import org.avario.utils.filters.impl.IIRFilter;
import org.avario.utils.filters.sensors.BaroSensorFilter;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class BaroSensorThread extends SensorThread<Float> {
	private BaroSensorFilter baroFilter = new BaroSensorFilter();
	private Filter preFilterPresure = new IIRFilter(0.7f - Preferences.baro_sensitivity * 0.01f);

	public BaroSensorThread(Activity activity) {
		super(activity);
		init();
	}

	protected void init() {
		sensors = new int[] { Sensor.TYPE_PRESSURE };
		sensorSpeed = SensorManager.SENSOR_DELAY_FASTEST;
	}

	@Override
	public synchronized void notifySensorChanged(SensorEvent sensorEvent) {
		float currentPresure = sensorEvent.values.clone()[0];
		currentPresure = preFilterPresure.doFilter(currentPresure)[0];
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
	}
}
