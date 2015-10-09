package org.avario.engine.sensors;

import org.avario.utils.StringFormatter;

import android.os.SystemClock;

public interface MovementFactor {

	static class Sample {
		public long timestamp;
		public double x;
		public float y;

		public Sample(double x, float y) {
			this.x = x;
			this.y = y;
			this.timestamp = SystemClock.elapsedRealtime();
		}

		public String toString() {
			return "sample: x " + StringFormatter.multipleDecimals(x) + " y " + StringFormatter.multipleDecimals(y);
		}
	}

	public void reset();

	public void notify(double time, float value);

	public float getValue();

}
