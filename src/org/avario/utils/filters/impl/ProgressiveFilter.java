package org.avario.utils.filters.impl;

import org.avario.utils.filters.Filter;

public class ProgressiveFilter implements Filter {
	private float[] prev;
	private float step;

	public ProgressiveFilter(float step) {
		this.step = step;
	}

	public void setStep(float step) {
		this.step = step;
	}

	@Override
	public synchronized float[] doFilter(float... value) {
		float[] ret = new float[value.length];
		if (prev != null) {
			for (int i = 0; i < value.length; i++) {
				// Logger.get().log("Diff " + (prev[i] - value[i]));
				if (prev[i] > value[i]) {
					ret[i] = Math.abs(prev[i] - value[i]) > step ? prev[i] - step : value[i];
				} else {
					ret[i] = Math.abs(prev[i] - value[i]) > step ? prev[i] + step : value[i];
				}
			}
		}
		prev = ret;
		return ret;
	}

	@Override
	public synchronized void reset() {
		prev = null;
	}

}
