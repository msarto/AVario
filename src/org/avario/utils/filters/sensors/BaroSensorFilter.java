package org.avario.utils.filters.sensors;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.StringFormatter;
import org.avario.utils.UnitsConverter;
import org.avario.utils.filters.Filter;

import android.hardware.SensorManager;
import android.location.Location;
import android.widget.Toast;

public class BaroSensorFilter implements LocationConsumer {

	private Filter[] baroFilters;
	private volatile float referrencePresure = Preferences.ref_qnh;
	private boolean gpsAltitude = false;

	public BaroSensorFilter(Filter... filters) {
		SensorProducer.get().registerConsumer(this);
		baroFilters = filters;
	}

	// filter the pressure and transform it to altitude
	public synchronized float toAltitude(float currentPresure) {
		float lastPresureNotified = currentPresure;
		for (Filter filter : baroFilters) {
			lastPresureNotified = filter.doFilter(lastPresureNotified)[0];
		}

		DataAccessObject.get().setLastPresure(lastPresureNotified);
		if (referrencePresure != Preferences.ref_qnh) {
			resetFilters();
			// altitudeFilter.reset();
			DataAccessObject.get().resetVSpeed();
			referrencePresure = Preferences.ref_qnh;
		}
		float sensorAlt = SensorManager.getAltitude(referrencePresure, lastPresureNotified);
		return sensorAlt;
	}

	@Override
	public void notifyWithLocation(final Location location) {
		if (!gpsAltitude && location.hasAltitude() && DataAccessObject.get().getLastPresure() > 0f
				&& location.getAccuracy() < 10) {
			final double altitude = location.getAltitude();
			final double gHeight = DataAccessObject.get().getGeoidHeight();
			Logger.get().log("Adjust altitude with GPS Altitude " + altitude + " and geoidHeight " + gHeight);
			gpsAltitude = true;
			adjustAltitude(Math.max(0, altitude - gHeight));
			AVarioActivity.CONTEXT.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String altitudeChangeNotif = AVarioActivity.CONTEXT.getApplicationContext().getString(
							R.string.altitude_from_gps,
							StringFormatter.noDecimals(UnitsConverter.toPreferredShort((float) (altitude - gHeight))));
					Toast.makeText(AVarioActivity.CONTEXT, altitudeChangeNotif, Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	protected synchronized void adjustAltitude(double h) {
		float ref = SensorManager.PRESSURE_STANDARD_ATMOSPHERE + 100;
		// double h = location.getAltitude();
		// adjust the reference pressure until the pressure sensor
		// altitude match the gps altitude +-5m
		if (h > 0) {
			float lastPresure = DataAccessObject.get().getLastPresure();
			double delta = Math.abs(SensorManager.getAltitude(ref, lastPresure) - h);
			while (delta > 2 && ref > 0) {
				ref -= 0.1 * delta;
				delta = Math.abs(SensorManager.getAltitude(ref, lastPresure) - h);
			}
		} else {
			ref = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
		}
		resetFilters();
		// altitudeFilter.reset();
		DataAccessObject.get().resetVSpeed();
		referrencePresure = ref;
		Preferences.ref_qnh = referrencePresure;
	}

	private void resetFilters() {
		if (baroFilters != null) {
			for (Filter filter : baroFilters) {
				filter.reset();
			}
		}
	}
}
