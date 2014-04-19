package org.avario.engine.sounds.tones;

import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.AsyncTone;
import org.avario.engine.sounds.TonePlayer;

public class PrenotifyTone extends AsyncTone {
	private volatile boolean hadPrenotify = false;
	protected TonePlayer tonePlayer = new TonePlayer();

	@Override
	public void beep() {
		if (!hadPrenotify) {
			hadPrenotify = true;
			tonePlayer.play(4001, TonePlayer.ToneType.LOW);
			tonePlayer.stop();

			try {
				Thread.sleep(Math.round(Preferences.prenotify_interval));
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				hadPrenotify = false;
			}
		}
	}

}
