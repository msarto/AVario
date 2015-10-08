package org.avario.utils.filters.impl;

import org.avario.engine.datastore.DataAccessObject;

public class StabiloFilter extends IIRFilter {

	public StabiloFilter() {
		super(0.5f);
	}

	@Override
	public synchronized float[] doFilter(final float... nowValues) {
		float sensitivity = DataAccessObject.get().getSensitivity();
		filterFactor = Math.min(0.8f, sensitivity * 0.01f);
		if (previousValues == null) {
			previousValues = nowValues;
		}
		if (Math.abs(nowValues[0] - previousValues[0]) > 0.1) {
			return previousValues;
		}
		return super.doFilter(nowValues);
	}
}
