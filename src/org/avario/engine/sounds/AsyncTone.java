package org.avario.engine.sounds;

public abstract class AsyncTone {
	protected TonePlayer tonePlayer = new TonePlayer();
	protected float beepSpeed;
	protected float beepHz;

	public abstract void beep();

	public void setHz(float hertz) {
		this.beepHz = hertz;
	}

	public void setSpeed(float beepSpeed) {
		this.beepSpeed = beepSpeed;
	}
}
