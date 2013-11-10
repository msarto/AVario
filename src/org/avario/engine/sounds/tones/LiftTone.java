package org.avario.engine.sounds.tones;

import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.AsyncTone;
import org.avario.engine.sounds.TonePlayer;
import org.avario.utils.Logger;

public class LiftTone extends AsyncTone {

	@Override
	public void beep() {
		if (!isPlaying) {
			try {
				isPlaying = true;
				tonePlayer.play(beepHz, TonePlayer.ToneType.HIGH);
				Thread.sleep(Math.round(Preferences.beep_interval - (Preferences.beep_interval / 6) * beepSpeed));
				tonePlayer.stop();
			} catch (InterruptedException e) {
				Logger.get().log("Fail lift beep ", e);
			} finally {
				isPlaying = false;
			}
		}
	}
}
