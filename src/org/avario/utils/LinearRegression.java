package org.avario.utils;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.impl.StabiloFilter;

public class LinearRegression {

	static class Sample {
		public double x;
		public float y;

		public Sample(double x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private volatile Queue<Sample> samples = new ArrayDeque<Sample>();
	private StabiloFilter filter = new StabiloFilter();
	private boolean needNewSlope = false;
	private float currentSlope = 0f;

	// / Invariants
	private double sumx;
	private float sumy;

	public synchronized void reset() {
		filter.reset();
		samples.clear();
		sumx = 0;
		sumy = 0f;
	}

	public synchronized void addSample(double x, float y, boolean replaceOld) {
		Sample newSample = new Sample(x, y);
		sumx += x;
		sumy += y;
		samples.add(newSample);
		needNewSlope = true;
		if (replaceOld) {
			// Cull old entries
			double oldest = x - (40 * Preferences.baro_sensitivity);
			while (samples.peek().x < oldest) {
				Sample s = samples.remove();
				sumx -= s.x;
				sumy -= s.y;
			}
		}
	}

	public synchronized float getSlope() {
		if (!needNewSlope) {
			return currentSlope;
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
		return currentSlope;
		// return beta1;
	}

	public synchronized float getLastDelta() {
		if (samples.size() > 1) {
			Sample last = null;
			Sample prev = null;
			while (samples.size() > 0) {
				prev = last;
				last = samples.remove();
			}

			if (prev != null && last != null) {
				samples.add(prev);
				samples.add(last);
				double deltaT = last.x - prev.x;
				float deltaD = last.y - prev.y;
				return (float) (deltaD / deltaT);
			}
		}
		return 0;
	}
}
