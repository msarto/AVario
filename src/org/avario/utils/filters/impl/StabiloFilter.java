package org.avario.utils.filters.impl;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.utils.filters.Filter;

public class StabiloFilter implements Filter {
	// The lower the noise is the higher the filtering is
	private volatile float previousValue = 0.0f;
	ProgressiveFilter progresive = new ProgressiveFilter(0);

	@Override
	public synchronized float[] doFilter(final float... value) {
		float sensitivity = DataAccessObject.get().getSensitivity();
		float ret = value.clone()[0];
		float delta = Math.abs(ret - previousValue);
		if (previousValue != 0.0f) {
			progresive.setStep(delta * (1 - 0.01f * sensitivity));
			ret = progresive.doFilter(ret)[0];
		}
		previousValue = ret;
		return new float[] { ret };
	}

	public synchronized void reset() {
		previousValue = 0.0f;
	}
}
