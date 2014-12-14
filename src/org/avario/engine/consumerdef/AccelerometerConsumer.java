package org.avario.engine.consumerdef;

public interface AccelerometerConsumer extends VarioConsumer {
	void notifyAcceleration(float x, float y, float z);
}
