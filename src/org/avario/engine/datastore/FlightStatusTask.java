package org.avario.engine.datastore;

import java.util.ArrayDeque;
import java.util.Queue;

import android.location.Location;

public class FlightStatusTask implements Runnable {

	private volatile boolean inFlight = false;
	protected Queue<Float> lastSpeeds = new ArrayDeque<Float>();
	private boolean bCanGo = false;
	private long lastSpeed = 0;
	private Thread th = new Thread(this);

	public void start() {
		bCanGo = true;
		th.start();
	}

	public void stop() throws InterruptedException {
		bCanGo = false;
		th.join(1000);
	}

	public boolean isInFlight() {
		return inFlight;
	}

	@Override
	public void run() {
		while (bCanGo) {
			Location lastLocation = DataAccessObject.get().getLastlocation();
			if (lastLocation != null && lastLocation.hasSpeed()) {
				if (lastLocation.getSpeed() > 3) {
					inFlight = true;
					lastSpeed = System.currentTimeMillis();
				} else if (inFlight && (System.currentTimeMillis() - lastSpeed) > 60000) {
					// The speed for 1 minute is less 8 km/h
					inFlight = false;
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
