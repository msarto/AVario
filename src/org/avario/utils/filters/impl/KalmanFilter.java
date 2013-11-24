package org.avario.utils.filters.impl;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.Filter;

public class KalmanFilter implements Filter {
	// Greetings to
	// http://trandi.wordpress.com/2011/05/16/kalman-filter-simplified-version/
	private float Q = 0.0001f;
	private float R = Preferences.baro_sensitivity * 0.0005f;
	private float P = Preferences.baro_sensitivity / 16f;
	private float X = 0f; // one dimensional
	private float K = 0;

	private void measurementUpdate() {
		K = (P + Q) / (P + Q + R);
		P = R * (P + Q) / (R + P + Q);
	}

	@Override
	public float[] doFilter(float... value) {
		float measurement = value[0];
		measurementUpdate();
		float result = X + (measurement - X) * K;
		X = result;
		return new float[] { result };
	}

	@Override
	public void reset() {
		Q = 0.0001f;
		R = Preferences.baro_sensitivity * 0.0005f;
		P = Preferences.baro_sensitivity / 16f;
		X = 0f;
	}
}
