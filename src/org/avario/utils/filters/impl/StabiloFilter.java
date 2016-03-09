package org.avario.utils.filters.impl;

import org.avario.engine.datastore.DataAccessObject;

public class StabiloFilter extends IIRFilter {
	private float sensitivity = DataAccessObject.get().getSensitivity();
	private MedianFixFilter last5 = new MedianFixFilter(Math.round(sensitivity * 0.2));

	public StabiloFilter() {
		super(0.5f);
	}

	@Override
	public synchronized float[] doFilter(final float... nowValues) {
		filterFactor = Math.min(0.9f, sensitivity * 0.01f);
		// increase the filter by 50% if there is a big gap or decrease it by
		// 50%
		float currentValue = nowValues[0];
		float median = last5.doFilter(currentValue)[0];
		float diff = Math.abs(median - currentValue);
		float corelation = Math.abs(diff * 100);
		filterFactor = 0.6f - filterFactor * Math.min(1.5f, corelation);
		if (previousValues == null) {
			previousValues = nowValues;
		}
		return super.doFilter(nowValues);
	}
}
