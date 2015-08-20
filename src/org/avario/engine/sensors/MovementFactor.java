package org.avario.engine.sensors;

import org.avario.utils.StringFormatter;

public interface MovementFactor {

	static class Sample {
		public double x;
		public float y;

		public Sample(double x, float y) {
			this.x = x;
			this.y = y;
		}

		public String toString() {
			return "sample: x " + StringFormatter.multipleDecimals(x) + " y " + StringFormatter.multipleDecimals(y);
		}
	}

	public void reset();

	public void notify(double time, float value);

	public float getValue();

}
