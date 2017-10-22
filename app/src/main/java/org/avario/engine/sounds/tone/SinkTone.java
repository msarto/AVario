package org.avario.engine.sounds.tone;

import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.AsyncTone;
import org.avario.utils.Logger;

public class SinkTone extends AsyncTone {
	protected TonePlayer tonePlayer = TonePlayer.get();
	protected float beepHz;

	public void setSpeed(float beepSpeed) {
		beepHz = Preferences.sink_hz + (Preferences.tone_variation * 0.3f) * beepSpeed;
		super.setSpeed(beepSpeed);
	}

	@Override
	public void beep() {
		if (!isPlaying) {
			try {
				isPlaying = true;
				tonePlayer.play(beepHz, TonePlayer.ToneType.HIGH);
				Thread.sleep(Math.round(Preferences.beep_interval - (Preferences.beep_interval / 6) * beepSpeed * -1));
				tonePlayer.stop();
			} catch (InterruptedException e) {
				Logger.get().log("Fail sink beep ", e);
			} finally {
				isPlaying = false;
			}
		}
	}

}
