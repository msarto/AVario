package org.avario.utils.filters.sensors;

import org.avario.engine.DataAccessObject;
import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.filters.Filter;
import org.avario.utils.filters.impl.MedianFixFilter;

import android.hardware.SensorManager;
import android.location.Location;

public class BaroSensorFilter implements LocationConsumer {
	private volatile float referrence = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
	// private MedianFixFilter baroFilter = new MedianFixFilter();
	private Filter baroFilter = new MedianFixFilter();
	// private IIRFilter altitudeFilter = new IIRFilter();

	private volatile float lastPresureNotified = -1f;
	private volatile boolean goodAccuracy = false;
	private volatile boolean gpsAltitude = false;

	public BaroSensorFilter() {
		Logger.get().log("Filter baro with % " + Preferences.baro_sensitivity);
		SensorProducer.get().registerConsumer(this);
	}

	// filter the pressure and transform it to altitude
	public synchronized float toAltitude(float currentPresure) {
		lastPresureNotified = currentPresure;
		lastPresureNotified = baroFilter.doFilter(lastPresureNotified)[0];
		float sensorAlt = SensorManager.getAltitude(referrence, lastPresureNotified);
		return sensorAlt;
		// return altitudeFilter.doFilter(sensorAlt)[0];
	}

	public float getReferrence() {
		return referrence;
	}

	@Override
	public void notifyWithLocation(Location location) {
		boolean betterAccuracy = !goodAccuracy && location.getAccuracy() > 0 && location.getAccuracy() < 5;
		if (betterAccuracy) {
			Logger.get().log("Better accuracy " + location.getAltitude());
			adjustAltitude(location);
			goodAccuracy = true;
			gpsAltitude = true;
		} else if (!gpsAltitude && location.hasAltitude() && lastPresureNotified > 0f) {
			Logger.get().log("GPS Altitude " + location.getAltitude());
			adjustAltitude(location);
			gpsAltitude = true;
		}
	}

	private synchronized void adjustAltitude(Location location) {
		float ref = SensorManager.PRESSURE_STANDARD_ATMOSPHERE + 100;
		double h = location.getAltitude();
		// adjust the reference pressure until the pressure sensor
		// altitude match the gps altitude +-5m
		while (Math.abs(SensorManager.getAltitude(ref, lastPresureNotified) - h) > 5 && ref > 0) {
			ref -= 0.5;
		}
		baroFilter.reset();
		// altitudeFilter.reset();
		DataAccessObject.get().resetVSpeed();
		referrence = ref;
	}
}
