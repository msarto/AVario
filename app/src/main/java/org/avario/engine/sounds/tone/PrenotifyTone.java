package org.avario.engine.sounds.tone;

import org.avario.engine.sounds.AsyncTone;

public class PrenotifyTone extends AsyncTone {
	private volatile boolean hadPrenotify = false;
	protected TonePlayer tonePlayer = TonePlayer.get();

	@Override
	public void beep() {
		if (!hadPrenotify) {
			hadPrenotify = true;
			tonePlayer.play(4001, TonePlayer.ToneType.LOW);

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				tonePlayer.stop();
				hadPrenotify = false;
			}
		}
	}

}
