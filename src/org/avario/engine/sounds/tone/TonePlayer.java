package org.avario.engine.sounds.tone;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;

import android.media.AudioFormat;
import android.media.AudioTrack;

public class TonePlayer {
	private static final TonePlayer THIS = new TonePlayer();
	private final float duration = 0.400f; // seconds
	private final int sampleRate = 8000;
	private final int numSamples = (int) (duration * sampleRate);
	private AudioTrack audioTrack = new AudioTrack(Preferences.STREAM_TYPE, sampleRate,
			AudioFormat.CHANNEL_OUT_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, numSamples * 2, AudioTrack.MODE_STATIC);
	private final int highTone = 32767;
	private final int lowTone = 65533;

	private TonePlayer() {

	}

	public static TonePlayer get() {
		return THIS;
	}

	public static enum ToneType {
		HIGH, LOW
	};

	public synchronized void play(float freqEndTone, ToneType type) {
		if (audioTrack != null) {
			// There must be something playing
			return;
		}
		int idx = 0;
		byte toneSound[] = new byte[2 * numSamples];
		int typeAmp = type == ToneType.HIGH ? highTone : lowTone;
		for (int i = 0; i < numSamples; ++i) {
			double dVal = Math.sin(2 * Math.PI * i / (sampleRate / freqEndTone));
			// scale to maximum amplitude
			final short val = (short) ((dVal * typeAmp));
			// in 16 bit wav PCM, first byte is the low order byte
			toneSound[idx++] = (byte) (val & 0x00ff);
			toneSound[idx++] = (byte) ((val & 0xff00) >>> 8);
		}
		try {
			audioTrack = new AudioTrack(Preferences.STREAM_TYPE, sampleRate, AudioFormat.CHANNEL_OUT_DEFAULT,
					AudioFormat.ENCODING_PCM_16BIT, numSamples * 2, AudioTrack.MODE_STATIC);
			audioTrack.write(toneSound, 0, toneSound.length);
			audioTrack.play();
		} catch (Throwable ex) {
			Logger.get().log("Fail playing track ", ex);
		}
	}

	public synchronized void stop() {
		try {
			if (audioTrack != null) {
				switch (audioTrack.getPlayState()) {
				case AudioTrack.PLAYSTATE_PAUSED:
				case AudioTrack.PLAYSTATE_PLAYING:
					audioTrack.pause();
					audioTrack.release();
					break;
				}
				audioTrack = null;
			}
		} catch (Throwable ex) {
			Logger.get().log("Fail stopping track ", ex);
		}
	}

}