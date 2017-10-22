package org.avario.engine.sounds.wav;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.AsyncTone;
import org.avario.utils.Logger;

import android.util.SparseIntArray;

public class WavLiftTone extends AsyncTone {

	private SparseIntArray listSounds = new SparseIntArray(5);
	private float prevBeepSpeed = 0;

	public WavLiftTone() {
		listSounds.put(0, player.load(AVarioActivity.CONTEXT, R.raw.l650, 1));
		listSounds.put(1, player.load(AVarioActivity.CONTEXT, R.raw.l700, 1));
		listSounds.put(2, player.load(AVarioActivity.CONTEXT, R.raw.l750, 1));
		listSounds.put(3, player.load(AVarioActivity.CONTEXT, R.raw.l800, 1));
		listSounds.put(4, player.load(AVarioActivity.CONTEXT, R.raw.l850, 1));
		listSounds.put(5, player.load(AVarioActivity.CONTEXT, R.raw.l900, 1));
	}

	@Override
	public void beep() {
		int iSoundId = listSounds.get(Math.round(beepSpeed));
		try {
			if (!isPlaying) {
				isPlaying = true;
				player.play(iSoundId, 1f, 1f, 1, 0, 1);
				Thread.sleep(Math.round(Preferences.beep_interval - (Preferences.beep_interval / 6) * beepSpeed));
			}
		} catch (InterruptedException e) {
			Logger.get().log("Fail lift beep ", e);
		} finally {
			player.stop(iSoundId);
			isPlaying = false;
		}
	}

	protected float getOffset() {
		float offset = 1f;
		offset = 0.5f + Math.min(1, Math.abs(prevBeepSpeed - beepSpeed));
		prevBeepSpeed = beepSpeed;
		return offset;
	}

	@Override
	public void stop() {
		player.release();
	}

}
