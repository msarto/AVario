package org.avario.utils.filters.impl;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.Filter;

public class StabiloFilter implements Filter {
	// The lower the noise is the higher the filtering is
	private float previousValue = 0.0f;
	protected float stabiloMinNoise = 0.01f / 1000f;

	@Override
	public synchronized float[] doFilter(final float... value) {
		float ret = value[0];
		float stabiloMaxNoise = (0.5f - Preferences.baro_sensitivity * 0.003f) / 1000f;
		float delta = Math.abs(ret - previousValue);
		if (delta > 0.001f) {
			previousValue = ret;
			return new float[] { 0 };
		}

		float deltaSign = (ret - previousValue) < 0 ? -1 : 1;
		if (previousValue != 0.0f && delta > stabiloMinNoise) {
			// noise > MIN
			if (delta > stabiloMaxNoise) {
				// noise > MAX
				ret = previousValue + deltaSign * stabiloMaxNoise;
			} else {
				// Noise between MIN and MAX
				ret = previousValue + deltaSign * delta
						* (1 - (delta - stabiloMinNoise) / (stabiloMaxNoise - stabiloMinNoise));
			}
			// Logger.get().log("Filter " + (value[0] * 1000) + " to " + (ret * 1000) + " delta " + delta);
		}
		previousValue = ret;
		return new float[] { ret };
	}

	public synchronized void reset() {
		previousValue = 0.0f;
	}
}
