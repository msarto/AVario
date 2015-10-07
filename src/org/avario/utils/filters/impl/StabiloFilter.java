package org.avario.utils.filters.impl;

import org.avario.engine.datastore.DataAccessObject;

public class StabiloFilter extends IIRFilter {

	public StabiloFilter() {
		super(0.5f);
		previousValues = new float[] { 0f };
	}

	@Override
	public synchronized float[] doFilter(final float... value) {
		float sensitivity = DataAccessObject.get().getSensitivity();
		filterFactor = Math.min(0.5f, 1 - sensitivity * 0.01f);
		float[] nowValues = value.clone();
		if (nowValues == null) {
			nowValues = previousValues;
		}
		float ratio = Math.abs(nowValues[0] - previousValues[0]) / previousValues[0];
		if (ratio > 0.1f) {
			filterFactor = Math.max(0.005f, filterFactor - filterFactor * ratio);
		} else {
			filterFactor = Math.min(0.5f, filterFactor + filterFactor * ratio);
		}
		previousValues = nowValues == null ? previousValues : nowValues;
		float[] newValues = super.doFilter(nowValues);
		return newValues;

	}

}
