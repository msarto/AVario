package org.avario.engine.sounds;

import org.avario.engine.prefs.Preferences;

import android.media.SoundPool;

public abstract class AsyncTone {
	protected float beepSpeed;
	protected static volatile boolean isPlaying = false;
	protected static SoundPool player = new SoundPool(20, Preferences.STREAM_TYPE, 0);

	public abstract void beep();

	public void setSpeed(float beepSpeed) {
		this.beepSpeed = beepSpeed;
	}

	public void stop() {
		beepSpeed = 0;
	}
}
