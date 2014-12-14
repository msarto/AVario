package org.avario.utils.filters.impl;

import org.avario.utils.filters.Filter;

public class IIRFilter implements Filter {
	private float[] previousValues;
	private float filterFactor = 0.5f;

	public IIRFilter(float filterFactor) {
		this.filterFactor = filterFactor;
	}

	@Override
	public synchronized float[] doFilter(float... value) {
		float[] ret = value.clone();
		if (previousValues != null) {
			for (int i = 0; i < previousValues.length; i++) {
				ret[i] = ret[i] * filterFactor + (1 - filterFactor)
						* previousValues[i];
			}
		}
		previousValues = ret;
		return ret;
	}

	public synchronized void reset() {
		previousValues = null;
	}

}
