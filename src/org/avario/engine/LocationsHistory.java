package org.avario.engine;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.prefs.Preferences;

import android.app.Activity;
import android.location.Location;

public class LocationsHistory implements LocationConsumer {
	private static LocationsHistory THIS;

	protected volatile Queue<Location> lastLocations = new ArrayDeque<Location>();
	protected volatile double gain = 0;

	protected LocationsHistory() {

	}

	public static void init(Activity context) {
		THIS = new LocationsHistory();
		SensorProducer.get().registerConsumer(THIS);
	}

	public static LocationsHistory get() {
		return THIS;
	}

	public Iterator<Location> getLocations() {
		synchronized (THIS) {
			return lastLocations.iterator();
		}
	}

	public double getHistoryAltimeterGain() {
		return gain;
	}

	@Override
	public void notifyWithLocation(Location location) {
		Location newLocation = (DataAccessObject.get().getLastlocation());
		synchronized (THIS) {
			if (lastLocations.size() > 0) {
				Location oldest = lastLocations.size() >= Preferences.location_history ? lastLocations.poll() : lastLocations.peek();
				if (oldest.hasAltitude() && newLocation.hasAltitude()) {
					gain = (newLocation.getAltitude() - oldest.getAltitude());
				}
			}
			lastLocations.add(newLocation);
		}
	}

	public void clearLocations() {
		synchronized (THIS) {
			lastLocations.clear();
			gain = 0;
		}
	}
}
