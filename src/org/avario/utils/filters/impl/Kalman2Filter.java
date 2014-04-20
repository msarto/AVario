package org.avario.utils.filters.impl;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.Filter;

public class Kalman2Filter implements Filter {

	protected volatile Queue<Float> history = new ArrayDeque<Float>();
	private double filterSizeFactor = Preferences.baro_sensitivity;

	public Kalman2Filter(double medianSizeFactor) {
		this.filterSizeFactor = medianSizeFactor;
	}

	@Override
	public synchronized float[] doFilter(float... value) {
		float[] result = new float[1];
		history.add(value[0]);
		result[0] = doKalman2();
		if (history.size() > filterSizeFactor) {
			history.poll();
		}
		return result;
	}

	protected float doKalman2() {
		float median = 0;
		int N = history.size();
		if (N == 1) {
			return history.peek();
		}
		float I = 0;
		for (float item : history) {
			median += item * I;
			I = I + 1f / (N * (N - 1));
		}
		return median * 2;
	}

	public synchronized void reset() {
		history.clear();
	}

	public static void main(String[] args) {
		Kalman2Filter filter = new Kalman2Filter(2);
		filter.doFilter(1);
		filter.doFilter(3);
		filter.doFilter(5);
		filter.doFilter(1);
		filter.doFilter(1);
		filter.doFilter(1);
	}

}