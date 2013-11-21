package org.avario.utils.filters.impl;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.Filter;

public class IIRFilter implements Filter {
	private float previousValue = 0.0f;
	private float filterFactor = -1;

	public IIRFilter() {

	}

	public IIRFilter(float filterFactor) {
		this.filterFactor = filterFactor;
	}

	@Override
	public synchronized float[] doFilter(float... value) {
		float[] ret = value;
		if (previousValue != 0.0f) {
			float iirFilter = filterFactor > 0 ? filterFactor : 1f - (Preferences.baro_sensitivity * 2f) / 100f;
			float prevValue = previousValue;
			ret[0] = value[0] * iirFilter + (1 - iirFilter) * prevValue;

		}
		previousValue = ret[0];
		return ret;
	}

	public synchronized void reset() {
		previousValue = 0.0f;
	}

}
