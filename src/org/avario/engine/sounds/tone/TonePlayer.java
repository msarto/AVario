package org.avario.engine.sounds.tone;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;

import android.media.AudioFormat;
import android.media.AudioTrack;

public class TonePlayer {
	private final float duration = 0.400f; // seconds
	private final int sampleRate = 8000;
	private final int numSamples = (int) (duration * sampleRate);
	private AudioTrack audioTrack = new AudioTrack(Preferences.STREAM_TYPE, sampleRate,
			AudioFormat.CHANNEL_OUT_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, numSamples * 2, AudioTrack.MODE_STATIC);
	private final int highTone = 32767;
	private final int lowTone = 65533;

	public static enum ToneType {
		HIGH, LOW
	};

	public void close() {
		try {
			if (audioTrack != null) {
				if (audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
					audioTrack.pause();
					audioTrack.flush();
					audioTrack.release();
				}
			}
		} catch (Exception ex) {
			Logger.get().log("Fail closing track ", ex);
		}
	}

	public synchronized void play(float freqEndTone, ToneType type) {
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
			if (audioTrack != null) {
				switch (audioTrack.getPlayState()) {
				case AudioTrack.PLAYSTATE_PAUSED:
				case AudioTrack.PLAYSTATE_PLAYING:
					audioTrack.pause();
					audioTrack.flush();
					audioTrack.release();
					break;
				}
			}

			audioTrack = new AudioTrack(Preferences.STREAM_TYPE, sampleRate, AudioFormat.CHANNEL_OUT_DEFAULT,
					AudioFormat.ENCODING_PCM_16BIT, numSamples * 2, AudioTrack.MODE_STATIC);
			audioTrack.write(toneSound, 0, toneSound.length);
			audioTrack.play();
		} catch (Exception ex) {
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
					audioTrack.flush();
					audioTrack.release();
					break;
				}
			}
		} catch (Exception ex) {
			Logger.get().log("Fail stopping track ", ex);
		}
	}

}