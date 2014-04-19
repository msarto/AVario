package org.avario.engine.sounds;

public abstract class AsyncTone {
	protected float beepSpeed;
	protected static volatile boolean isPlaying = false;

	public abstract void beep();

	public void setSpeed(float beepSpeed) {
		this.beepSpeed = beepSpeed;
	}

	public void stop() {
		beepSpeed = 0;
	}
}
