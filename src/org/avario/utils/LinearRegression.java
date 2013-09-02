package org.avario.utils;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.engine.prefs.Preferences;

public class LinearRegression {

	static class Sample {
		public long x;
		public float y;

		public Sample(long x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private volatile Queue<Sample> samples = new ArrayDeque<Sample>();
	private static final Object sync = new Object();

	// / Invariants
	private long sumx;
	private float sumy;

	public void reset() {
		synchronized (sync) {
			samples.clear();
			sumx = 0;
			sumy = 0f;
		}
	}

	public void addSample(long x, float y) {
		synchronized (sync) {
			Sample newSample = new Sample(x, y);
			sumx += x;
			sumy += y;
			samples.add(newSample);

			// Cull old entries
			long oldest = x - (100 + 45 * Preferences.baro_sensitivity);
			while (samples.peek().x < oldest) {
				Sample s = samples.remove();
				sumx -= s.x;
				sumy -= s.y;
			}
		}
	}

	public float getSlope() {
		synchronized (sync) {
			if (samples.size() > 0) {
				float xbar = sumx / (float) samples.size();
				float ybar = sumy / (float) samples.size();
				float xxbar = 0.0f, xybar = 0.0f;
				for (Sample s : samples) {
					xxbar += (s.x - xbar) * (s.x - xbar);
					xybar += (s.x - xbar) * (s.y - ybar);
				}
				float beta1 = xybar / xxbar;
				return beta1;
			}
		}
		return 0;
	}
}
