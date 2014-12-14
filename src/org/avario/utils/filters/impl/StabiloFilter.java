package org.avario.utils.filters.impl;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.utils.filters.Filter;

public class StabiloFilter implements Filter {
	// The lower the noise is the higher the filtering is
	private volatile float previousValue = 0.0f;

	public StabiloFilter() {

	}

	@Override
	public float[] doFilter(final float... value) {
		float factor = 0.7f - DataAccessObject.get().getSensitivity();
		float ret = value.clone()[0];		
		float stabiloMaxNoise = ((0.2f - DataAccessObject.get().getSensitivity() * 0.003f) / 1000f) * factor;
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
