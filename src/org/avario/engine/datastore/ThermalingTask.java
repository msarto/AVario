package org.avario.engine.datastore;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;

import android.location.Location;

public class ThermalingTask implements Runnable {
	private volatile Thread blinker;
	private Thread thr;
	private Location lastThermal;

	ThermalingTask() {
		thr = new Thread(this);
	}

	public void start() {
		thr.start();
	}

	public void stop() {
		blinker = null;
	}

	public Location getLastThermal() {
		return lastThermal;
	}

	@Override
	public void run() {
		try {
			Logger.get().log(
					"Start thermal interval: " + Preferences.min_thermal_interval + "; min gain: "
							+ Preferences.min_thermal_gain);
			blinker = Thread.currentThread();
			while (blinker == Thread.currentThread()) {
				float startAltitude = DataAccessObject.get().getLastAltitude();
				Thread.sleep(Math.round(Preferences.min_thermal_interval));
				float endAltitude = DataAccessObject.get().getLastAltitude();
				if (startAltitude > 0 && (startAltitude + Preferences.min_thermal_gain <= endAltitude)) {
					lastThermal = DataAccessObject.get().getLastlocation();
				}
			}
		} catch (Exception e) {
			Logger.get().log("Thermal task exception ", e);
		} finally {
			Logger.get().log("Thermal task terminated");
		}
	}
}
