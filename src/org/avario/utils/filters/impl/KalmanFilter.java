package org.avario.utils.filters.impl;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.Filter;

public class KalmanFilter implements Filter {

	// Greetings to
	// http://trandi.wordpress.com/2011/05/16/kalman-filter-simplified-version/
	private float Q = Preferences.baro_sensitivity * 0.000004f;
	private float R = Preferences.baro_sensitivity * 0.0006f;
	private float P = Preferences.baro_sensitivity / (2f * 1000f);
	private float X = 0; // one dimensional
	private float K = 0;
	private float sensitivity = Preferences.baro_sensitivity;

	public KalmanFilter(float sensitivity) {
		this.sensitivity = sensitivity;
		Q = sensitivity * 0.000004f;
		R = sensitivity * 0.0006f;
		P = sensitivity / (2f * 1000f);
		X = 0; // one dimensional
		K = 0;
	}

	@Override
	public float[] doFilter(float... value) {
		float measurement = value[0];
		if (X == 0) {
			X = measurement;
		}
		K = (P + Q) / (P + Q + R);
		P = R * K;
		X = X + (measurement - X) * K;
		return new float[] { X };
	}

	@Override
	public void reset() {
		Q = sensitivity * 0.000004f;
		R = sensitivity * 0.0006f;
		P = sensitivity / (2f * 1000f);
		X = 0f;
	}
}
