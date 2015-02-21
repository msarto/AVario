package org.avario.engine.datastore;

import java.util.ArrayDeque;
import java.util.Queue;

import android.location.Location;

public class FlightStatusTask implements Runnable {
	protected static final Object lock = new Object();

	private volatile boolean inFlight = false;
	protected Queue<Float> lastSpeeds = new ArrayDeque<Float>();
	private boolean bCanGo = false;
	private float mean = 0;

	public void start() {
		bCanGo = true;
		new Thread(this).start();
	}

	public void stop() {
		bCanGo = false;
	}

	public void reset() {
		synchronized (lock) {
			lastSpeeds.clear();
		}
	}

	public boolean isInFlight() {
		return inFlight;
	}

	@Override
	public void run() {
		while (bCanGo) {
			Location lastLocation = DataAccessObject.get().getLastlocation();
			if (lastLocation != null && lastLocation.hasSpeed()) {
				synchronized (lock) {
					if (lastLocation.getSpeed() > 3 && mean > 2) {
						inFlight = true;
					} else if (lastLocation.getSpeed() < 1 && mean < 1) {
						inFlight = false;
					}
					mean = lastLocation.getSpeed() * 0.3f + mean * 0.7f;
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
