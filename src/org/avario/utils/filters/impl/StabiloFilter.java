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
		float factor = Math.min(0.5f, 1 - sensitivity * 0.01f);
		setFactor(factor);
		return super.doFilter(value);
	}

}
