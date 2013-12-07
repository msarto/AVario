package org.avario.utils.filters.sensors;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.DataAccessObject;
import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.prefs.Preferences;
import org.avario.engine.tracks.Tracker;
import org.avario.utils.Logger;
import org.avario.utils.StringFormatter;
import org.avario.utils.UnitsConverter;
import org.avario.utils.filters.Filter;
import org.avario.utils.filters.impl.KalmanFilter;

import android.hardware.SensorManager;
import android.location.Location;
import android.widget.Toast;

public class BaroSensorFilter implements LocationConsumer {
	private volatile float referrence = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
	private Filter baroFilter = new KalmanFilter();

	private volatile float lastPresureNotified = -1f;
	private volatile boolean gpsAltitude = false;

	public BaroSensorFilter() {
		Logger.get().log("Filter baro with % " + Preferences.baro_sensitivity);
		SensorProducer.get().registerConsumer(this);
	}

	// filter the pressure and transform it to altitude
	public synchronized float toAltitude(float currentPresure) {
		lastPresureNotified = baroFilter.doFilter(currentPresure)[0];
		float sensorAlt = SensorManager.getAltitude(referrence, lastPresureNotified);
		return sensorAlt;
	}

	@Override
	public void notifyWithLocation(Location location) {
		if (!gpsAltitude && location.hasAltitude() && lastPresureNotified > 0f && location.getAccuracy() < 5) {
			Logger.get().log("GPS Altitude " + location.getAltitude());
			adjustAltitude(location);
			gpsAltitude = true;
			// Start the track if selected
			if (Preferences.auto_track && !Tracker.get().isTracking()) {
				AVarioActivity.startAutoTrack();
			}
		}
	}

	private synchronized void adjustAltitude(Location location) {
		float ref = SensorManager.PRESSURE_STANDARD_ATMOSPHERE + 100;
		double h = location.getAltitude();
		String altitudeChangeNotif = AVarioActivity.CONTEXT.getApplicationContext().getString(
				R.string.altitude_from_gps, StringFormatter.noDecimals(UnitsConverter.toPreferredShort((float) h)));

		Toast.makeText(AVarioActivity.CONTEXT, altitudeChangeNotif, Toast.LENGTH_LONG).show();
		// adjust the reference pressure until the pressure sensor
		// altitude match the gps altitude +-5m
		double delta = Math.abs(SensorManager.getAltitude(ref, lastPresureNotified) - h);
		while (delta > 2 && ref > 0) {
			ref -= 0.1 * delta;
			delta = Math.abs(SensorManager.getAltitude(ref, lastPresureNotified) - h);
		}
		baroFilter.reset();
		// altitudeFilter.reset();
		DataAccessObject.get().resetVSpeed();
		referrence = ref;
	}
}
