package org.avario.utils.filters.sensors;

import org.avario.utils.filters.Filter;
import org.avario.utils.filters.impl.ProgressiveFilter;

import android.hardware.SensorManager;

public class CompasSensorFilter {
	private Filter progressiveMove;

	public CompasSensorFilter(float sensitivity) {
		progressiveMove = new ProgressiveFilter(sensitivity);
	}

	private float[] acclerometerValues = new float[] { 0, 0, 0, 0 };
	private Filter accelerometerFilter = new ProgressiveFilter(5);

	// filter the pressure and transform it to altitude
	public float toBearing(float[] current) {
		final float[] R = new float[16];
		final float[] actual_orientation = new float[3];
		SensorManager.getRotationMatrix(R, null, acclerometerValues, current);
		SensorManager.getOrientation(R, actual_orientation);
		float actualCompasNotified = -actual_orientation[0] * 360.0f / (2 * 3.1415926535f);
		return actualCompasNotified;
	}

	public float smoothFilter(final float rawValue) {
		return progressiveMove.doFilter(rawValue)[0];
	}

	public void notifyAccelerometer(float[] values) {
		acclerometerValues = (accelerometerFilter != null) ? accelerometerFilter.doFilter(values) : values;
	}
}
