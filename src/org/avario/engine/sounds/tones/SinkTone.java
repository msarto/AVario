package org.avario.engine.sounds.tones;

import org.avario.engine.sounds.AsyncTone;
import org.avario.engine.sounds.TonePlayer;

public class SinkTone extends AsyncTone {
	private volatile boolean isPlaying = false;

	@Override
	public void beep() {
		if (!isPlaying) {
			isPlaying = true;
			tonePlayer.play(beepHz, TonePlayer.ToneType.HIGH);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tonePlayer.stop();
			isPlaying = false;
		}
	}


}
