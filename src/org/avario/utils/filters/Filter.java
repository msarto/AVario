package org.avario.utils.filters;

public interface Filter {

	float[] doFilter(float... value);

	void reset();
}
