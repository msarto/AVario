package org.avario.engine.sensors;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.impl.StabiloFilter;

public class LinearRegression implements MovementFactor {

	private volatile Queue<Sample> samples = new ArrayDeque<Sample>();
	private StabiloFilter filter = new StabiloFilter();
	private boolean needNewSlope = false;
	private float currentSlope = 0f;

	// / Invariants
	private double sumx;
	private float sumy;

	@Override
	public synchronized void reset() {
		filter.reset();
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
		double oldest = x - (40 * Preferences.baro_sensitivity);
		while (samples.peek().x < oldest) {
			Sample s = samples.remove();
			sumx -= s.x;
			sumy -= s.y;
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
		float beta1 = xybar / xxbar;

		currentSlope = filter.doFilter(beta1)[0];
		return currentSlope * 1000f;
	}

}
