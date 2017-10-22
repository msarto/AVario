package org.avario.utils.filters.impl;

import org.avario.utils.filters.Filter;

public class KalmanFilter implements Filter {

	// Greetings to
	// http://trandi.wordpress.com/2011/05/16/kalman-filter-simplified-version/
	private float sensitivity = 25;

	private float Q = sensitivity * 0.0004f;
	private float R = sensitivity * 0.006f;
	private float P = sensitivity / 2000f;
	private float X = 0; // one dimensional
	private float K = 0;

	public KalmanFilter(float sensitivity) {
		this.sensitivity = sensitivity;
		Q = sensitivity * 0.0005f;
		R = sensitivity * 0.009f;
		P = sensitivity / 2000f;
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
		Q = sensitivity * 0.0004f;
		R = sensitivity * 0.006f;
		P = sensitivity / 2000f;
		X = 0f;
	}
}
