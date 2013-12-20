package org.avario.engine.sensors.bt;

import org.avario.engine.sensors.MovementFactor;

public class OutsideFactor implements MovementFactor {

	private float value = 0f; 

	@Override
	public void reset() {
		value = 0f;
		
	}

	@Override
	public void notify(double time, float value) {
		this.value = value;
		
	}

	@Override
	public float getValue() {
		return value;
	}

}
