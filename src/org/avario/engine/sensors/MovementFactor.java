package org.avario.engine.sensors;

public interface MovementFactor {
	
	static class Sample {
		public double x;
		public float y;

		public Sample(double x, float y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public void reset();

	public void notify(double time, float value);

	public float getValue();

}
