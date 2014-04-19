package org.avario.engine.sounds.tones.wav;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.AsyncTone;
import org.avario.utils.Logger;

import android.media.MediaPlayer;
import android.util.SparseArray;

public class WavLiftToneRev extends AsyncTone {
	private SparseArray<MediaPlayer> listSounds = new SparseArray<MediaPlayer>(5);
	private float prevBeepSpeed = 0;

	public WavLiftToneRev() {
		listSounds.put(0, MediaPlayer.create(AVarioActivity.CONTEXT, R.raw.l1000));
		listSounds.put(1, MediaPlayer.create(AVarioActivity.CONTEXT, R.raw.l1200));
		listSounds.put(2, MediaPlayer.create(AVarioActivity.CONTEXT, R.raw.l1300));
		listSounds.put(3, MediaPlayer.create(AVarioActivity.CONTEXT, R.raw.l1400));
		listSounds.put(4, MediaPlayer.create(AVarioActivity.CONTEXT, R.raw.l1600));
		listSounds.put(5, MediaPlayer.create(AVarioActivity.CONTEXT, R.raw.l1700));
	}

	@Override
	public void beep() {
		try {
			if (!isPlaying) {
				isPlaying = true;
				MediaPlayer player = listSounds.get(Math.round(beepSpeed));

				// PresetReverb pReverb = new PresetReverb(1, 0);
				//player.attachAuxEffect(pReverb.getId());
				//pReverb.setPreset(PresetReverb.PRESET_LARGEROOM);
				//pReverb.setEnabled(true);
				//player.setAuxEffectSendLevel(1.0f);
				player.start();
				Thread.sleep(Math.max(300,
						Math.round(Preferences.beep_interval - (Preferences.beep_interval / 6) * beepSpeed)));
				//player.stop();
				//player.reset();
			}

		} catch (Exception e) {
			Logger.get().log("Fail lift beep ", e);
		} finally {
			isPlaying = false;
		}
	}

	public float getOffset() {
		float offset = 1f;
		offset = 0.5f + Math.min(1, Math.abs(prevBeepSpeed - beepSpeed));
		prevBeepSpeed = beepSpeed;
		return offset;
	}
}
