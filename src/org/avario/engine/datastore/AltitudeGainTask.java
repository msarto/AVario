package org.avario.engine.datastore;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.engine.prefs.Preferences;

public class AltitudeGainTask implements Runnable {
	private volatile Thread blinker;
	private Thread thr;
	protected volatile Queue<Float> lastLocations = new ArrayDeque<Float>();
	protected float gain = 0f;

	AltitudeGainTask() {
		thr = new Thread(this);
	}

	public void start() {
		thr.start();
	}

	public void stop() {
		blinker = null;
	}

	public void reset() {
		lastLocations.clear();
	}

	public float getAltitudeGain() {
		return gain;
	}

	@Override
	public void run() {
		blinker = Thread.currentThread();
		while (blinker == Thread.currentThread()) {
			if (DataAccessObject.get().getLastAltitude() > 0) {
				if (lastLocations.size() > 0) {
					float oldLocationAltitude = (lastLocations.size() >= Preferences.location_history) ? lastLocations
							.poll() : lastLocations.peek();
					gain = DataAccessObject.get().getLastAltitude() - oldLocationAltitude;
				}
				lastLocations.add(DataAccessObject.get().getLastAltitude());
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				blinker = null;
			}
		}
	}

}
