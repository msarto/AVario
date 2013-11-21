package org.avario.utils.filters.impl;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.filters.Filter;

public class StabiloFilter implements Filter {
	// The lower the noise is the higher the filtering is
	private float previousValue = 0.0f;
	protected static float stabiloMinNoise = 0.1f / 1000f;
	protected static float stabiloMaxNoise = 0.5f / 1000f;

	@Override
	public float[] doFilter(float... value) {
		float ret = value[0];
		float delta = Math.abs(ret - previousValue);
		if (delta > 0.02f) {
			return new float[] { 0 };
		}
		stabiloMaxNoise = Math.max(0.2f, 0.02f * Preferences.baro_sensitivity) / 1000f;
		float deltaSign = (ret - previousValue) < 0 ? -1 : 1;
		if (previousValue != 0.0f && stabiloMinNoise < delta) {
			// noise > MIN
			if (delta > stabiloMaxNoise) {
				// noise > MAX
				ret = previousValue + deltaSign * stabiloMaxNoise;
			} else {
				// Noise between MIN and MAX
				ret = previousValue + deltaSign * delta
						* (1 - (delta - stabiloMinNoise) / (stabiloMaxNoise - stabiloMinNoise));
			}
			Logger.get().log("Filter " + (value[0] * 1000) + " to " + (ret * 1000));
		}
		previousValue = ret;
		return new float[] { ret };
	}

	public void reset() {
		previousValue = 0.0f;
	}
}
