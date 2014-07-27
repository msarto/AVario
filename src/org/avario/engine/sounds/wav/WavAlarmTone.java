package org.avario.engine.sounds.wav;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.sounds.AsyncTone;
import org.avario.utils.Logger;

import android.util.SparseIntArray;

public class WavAlarmTone extends AsyncTone {
	private SparseIntArray listSounds = new SparseIntArray(1);

	public WavAlarmTone() {
		listSounds.put(0, player.load(AVarioActivity.CONTEXT, R.raw.buzzer, 1));
	}

	@Override
	public void beep() {
		int iSoundId = listSounds.get(0);
		try {
			if (isPlaying) {
				player.autoPause();
			}
			isPlaying = true;
			player.play(iSoundId, 1f, 1f, 0, 0, 1);
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Logger.get().log("Fail lift beep ", e);
		} finally {
			player.stop(iSoundId);
			player.autoResume();
			isPlaying = false;
		}
	}

	@Override
	public void stop() {
		player.release();
	}

}
