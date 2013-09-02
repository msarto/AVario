package org.avario.engine.sounds.tones;

import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.AsyncTone;
import org.avario.engine.sounds.TonePlayer;

public class LiftTone extends AsyncTone {
	private volatile boolean isPlaying = false;

	@Override
	public void beep() {
		if (!isPlaying) {
			isPlaying = true;
			tonePlayer.play(beepHz, TonePlayer.ToneType.HIGH);
			try {
				Thread.sleep(Math.round(Preferences.beep_interval - (Preferences.beep_interval / 6) * beepSpeed));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tonePlayer.stop();
			isPlaying = false;
		}
	}

}
