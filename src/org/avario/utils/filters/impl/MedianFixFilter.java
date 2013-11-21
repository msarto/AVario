package org.avario.utils.filters.impl;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.Filter;

public class MedianFixFilter implements Filter {
	private volatile float medianValue = 0f;
	protected volatile Queue<Float> history = new ArrayDeque<Float>();
	private double medianSizeFactor = 0.8;

	public MedianFixFilter() {

	}

	public MedianFixFilter(double medianSizeFactor) {
		this.medianSizeFactor = medianSizeFactor;
	}

	@Override
	public synchronized float[] doFilter(float... value) {
		float[] result = new float[1];
		history.add(value[0]);
		float peak = 0f;
		if (history.size() > Preferences.baro_sensitivity * medianSizeFactor) {
			peak = history.poll();
		}
		result[0] = medianDoMedian(peak, value[0]);
		return result;
	}

	protected float medianDoMedian(float peak, float newOne) {
		if (peak > 0f) {
			medianValue -= peak;
		}
		medianValue += newOne;
		return medianValue / (float) history.size();
	}

	public void reset() {
		history.clear();
		medianValue = 0;
	}
}
