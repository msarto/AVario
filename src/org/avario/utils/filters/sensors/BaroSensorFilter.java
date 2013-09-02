package org.avario.utils.filters.sensors;

import org.avario.engine.DataAccessObject;
import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.filters.impl.IIRFilter;
import org.avario.utils.filters.impl.MedianFixFilter;

import android.hardware.SensorManager;
import android.location.Location;

public class BaroSensorFilter implements LocationConsumer {
	private volatile float referrence = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
	private MedianFixFilter baroFilter = new MedianFixFilter();
	private IIRFilter altitudeFilter = new IIRFilter();

	private volatile float lastPresureNotified = -1f;
	private volatile boolean gpsadjusted = false;

	public BaroSensorFilter() {
		Logger.get().log("Filter baro with % " + Preferences.baro_sensitivity);
		SensorProducer.get().registerConsumer(this);
	}

	// filter the pressure and transform it to altitude
	public float toAltitude(float currentPresure) {
		lastPresureNotified = currentPresure;
		lastPresureNotified = baroFilter.doFilter(lastPresureNotified)[0];
		float sensorAlt = SensorManager.getAltitude(referrence, lastPresureNotified);
		return altitudeFilter.doFilter(sensorAlt)[0];
	}

	public float getReferrence() {
		return referrence;
	}

	@Override
	public void notifyWithLocation(Location location) {
		if (!gpsadjusted && location.hasAltitude() && lastPresureNotified > 0f) {
			float ref = SensorManager.PRESSURE_STANDARD_ATMOSPHERE + 100;
			double h = DataAccessObject.get().getGpsAltitude();
			// adjust the reference pressure until the pressure sensor
			// altitude match the gps altitude +-5m
			while (Math.abs(SensorManager.getAltitude(ref, lastPresureNotified) - h) > 5 && ref > 0) {
				ref -= 0.5;
			}
			baroFilter.reset();
			altitudeFilter.reset();
			DataAccessObject.get().resetVSpeed();
			referrence = ref;
			gpsadjusted = true;
		}
	}
}
