package org.avario.engine.datastore;

import org.avario.utils.Logger;

import android.location.Location;

public class HeadingTask implements Runnable {
	private volatile Thread blinker;
	private Thread thr;
	private Location headingReferrence;
	private float heading = -1f;

	HeadingTask() {
		thr = new Thread(this);
	}

	public void start() {
		thr.start();
	}

	public void stop() {
		blinker = null;
	}

	public float getHeading() {
		return heading;
	}

	@Override
	public void run() {
		try {
			blinker = Thread.currentThread();
			while (blinker == Thread.currentThread()) {
				Location lastLocation = DataAccessObject.get().getLastlocation();
				if (headingReferrence != null && lastLocation != null
						&& headingReferrence.getTime() < lastLocation.getTime()) {
					heading = headingReferrence.bearingTo(lastLocation);
				}
				headingReferrence = lastLocation;
				Thread.sleep(3000);
			}
		} catch (InterruptedException e) {
		} finally {
			Logger.get().log("Heading task terminated");
		}
	}
}
