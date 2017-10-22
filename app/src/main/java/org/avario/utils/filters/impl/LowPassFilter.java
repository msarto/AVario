package org.avario.utils.filters.impl;

import org.avario.utils.filters.Filter;

public class LowPassFilter implements Filter {
	protected float[] previousValues;
	protected float filterFactor = 0.5f;

	public LowPassFilter(float filterFactor) {
		if (filterFactor >= 0 && filterFactor <= 1) {
			this.filterFactor = filterFactor;
		}
	}

	public void setFactor(float factor) {
		if (filterFactor >= 0 && filterFactor <= 1) {
			this.filterFactor = factor;
		}
	}

	@Override
	public synchronized float[] doFilter(final float... value) {
		float[] ret = value.clone();
		if (previousValues != null) {
			for (int i = 0; i < previousValues.length; i++) {
				ret[i] = ret[i] + filterFactor * (previousValues[i] - ret[i]);
			}
		}
		previousValues = ret;
		return ret;
	}

	public synchronized void reset() {
		previousValues = null;
	}

}
