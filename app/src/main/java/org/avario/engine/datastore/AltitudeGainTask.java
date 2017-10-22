package org.avario.engine.datastore;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.engine.prefs.Preferences;

public class AltitudeGainTask implements Runnable {
	protected static final Object lock = new Object();

	private volatile boolean bCanGo = false;
	protected Queue<Double> lastLocations = new ArrayDeque<Double>();
	protected volatile double gain = 0f;

	AltitudeGainTask() {
	}

	public void start() {
		bCanGo = true;
		new Thread(this).start();
	}

	public void stop() {
		bCanGo = false;
	}

	public void reset() {
		synchronized (lock) {
			lastLocations.clear();
		}
	}

	public double getAltitudeGain() {
		return gain;
	}

	@Override
	public void run() {
		while (bCanGo) {
			if (DataAccessObject.get().getLastAltitude() > 0) {
				synchronized (lock) {
					if (lastLocations.size() > 0) {
						double oldLocationAltitude = (lastLocations.size() >= Preferences.location_history) ? lastLocations
								.poll() : lastLocations.peek();
						gain = DataAccessObject.get().getLastAltitude() - oldLocationAltitude;
					}
					lastLocations.add(DataAccessObject.get().getLastAltitude());
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				bCanGo = false;
			}
		}
	}
}
