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
			deltaD = adjustConsecutiveDeltaD(deltaD);
			return (float) (deltaD / deltaT) * 1000;
		}
		return 0;
	}

	private float adjustConsecutiveDeltaD(float deltaD) {
		if (1 < Math.abs(deltaD)) {
			deltaD = deltaD > 6 ? 6 : deltaD;
			deltaD = deltaD < -6 ? -6 : deltaD;
			return (float) (deltaD - Math.signum(deltaD) * Math.log(Math.abs(deltaD)));
		}
		return deltaD;
	}

	public static void main(String[] args) {
		GpsMovement m = new GpsMovement();
		m.notify(1000f, 1006.1f);
		m.notify(2000f, 10066.1f);
		System.out.println(m.getValue());
	}

}
