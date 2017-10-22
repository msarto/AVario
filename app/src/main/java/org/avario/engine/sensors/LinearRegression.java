package org.avario.engine.sensors;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.engine.datastore.DataAccessObject;

import android.os.SystemClock;

public class LinearRegression implements MovementFactor {

	private Queue<Sample> samples = new ArrayDeque<Sample>();
	private boolean needNewSlope = false;
	private float currentSlope = 0f;

	// / Invariants
	private double sumx;
	private float sumy;

	@Override
	public synchronized void reset() {
		samples.clear();
		sumx = 0;
		sumy = 0f;
	}

	@Override
	public synchronized void notify(double x, float y) {
		Sample newSample = new Sample(x, y);
		sumx += x;
		sumy += y;
		samples.add(newSample);
		needNewSlope = true;
		// Cull old entries
		double sensitivity = DataAccessObject.get().getSensitivity() * 10.0;
		boolean canSample = true;
		while (canSample && samples.size() > 2) {
			Sample s = samples.peek();
			if (SystemClock.elapsedRealtime() - s.timestamp > sensitivity) {
				samples.poll();
				sumx -= s.x;
				sumy -= s.y;
			} else {
				canSample = false;
			}
		}
	}

	@Override
	public synchronized float getValue() {
		if (!needNewSlope) {
			return currentSlope * 1000f;
		}

		needNewSlope = false;
		double xbar = sumx / (double) samples.size();
		float ybar = sumy / (float) samples.size();
		float xxbar = 0.0f, xybar = 0.0f;
		for (Sample s : samples) {
			xxbar += (s.x - xbar) * (s.x - xbar);
			xybar += (s.x - xbar) * (s.y - ybar);
		}
		currentSlope = xybar / xxbar;
		return currentSlope * 1000f;
	}

}
