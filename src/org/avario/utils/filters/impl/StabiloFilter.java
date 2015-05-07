package org.avario.utils.filters.impl;

import org.avario.engine.datastore.DataAccessObject;

public class StabiloFilter extends IIRFilter {
	private boolean init = false;

	public StabiloFilter() {
		super(0.5f);
		previousValues = new float[] { 0f };
		float sensitivity = DataAccessObject.get().getSensitivity();
		filterFactor = Math.min(0.5f, 1 - sensitivity * 0.01f);
	}

	@Override
	public synchronized float[] doFilter(final float... value) {
		float[] nowValues = value.clone();
		if (!init) {
			previousValues = nowValues;
			init = true;
		}
		float ratio = Math.abs(nowValues[0] - previousValues[0]) / previousValues[0];
		if (ratio > 0.1f) {
			filterFactor = Math.max(0.005f, filterFactor - filterFactor * ratio);
		} else {
			filterFactor = Math.min(0.5f, filterFactor + filterFactor * ratio);
		}
		float[] newValues = super.doFilter(nowValues);
		return newValues;

	}

}
