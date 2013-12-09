package org.avario.engine.sensors;

public class GpsMovement implements MovementFactor {

	private Sample last = null;
	private Sample prev = null;

	@Override
	public void reset() {
		prev = last = null;
	}

	@Override
	public void notify(double time, float value) {
		prev = last;
		last = new Sample(time, value);
	}

	@Override
	public float getValue() {
		if (prev != null && last != null) {
			double deltaT = last.x - prev.x;
			float deltaD = last.y - prev.y;
			return (float) (deltaD / deltaT) * 1000;
		}
		return 0;
	}

}
