package org.avario.utils.filters.impl;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.Filter;

public class StabiloFilter implements Filter {
	// The lower the noise is the higher the filtering is
	private float previousValue = 0.0f;
	protected float factor = 1;

	public StabiloFilter() {

	}

	public StabiloFilter(float factor) {
		this.factor = factor;
	}

	@Override
	public synchronized float[] doFilter(final float... value) {
		float ret = value[0];
		float stabiloMaxNoise = ((0.2f - Preferences.baro_sensitivity * 0.003f) / 1000f) * factor;
		final float stabiloMinNoise = 0.000003f * factor;
		float delta = Math.abs(ret - previousValue);
		if (delta > 0.001f * factor)/* 1mps */{
			previousValue = ret;
			return new float[] { previousValue };
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
		}
		previousValue = ret;
		return new float[] { ret };
	}

	public synchronized void reset() {
		previousValue = 0.0f;
	}
}
