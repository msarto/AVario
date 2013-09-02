package org.avario.engine.sounds;

import org.avario.utils.Logger;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class TonePlayer {
	private final float duration = 0.400f; // seconds
	private final int sampleRate = 8000;
	private final int numSamples = (int) (duration * sampleRate);
	private AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
			AudioFormat.ENCODING_PCM_16BIT, numSamples * 2, AudioTrack.MODE_STATIC);
	private boolean initialized = false;
	private final int highTone = 32767;
	private final int lowTone = 65533;

	public static enum ToneType {
		HIGH, LOW
	};

	public void close() {
		try {
			if (audioTrack != null) {
				if (audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
					audioTrack.stop();
				}
				audioTrack.release();
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
			createNative(toneSound, numSamples);
			play();
		} catch (Exception ex) {
			Logger.get().log("Fail playing track ", ex);
		}
	}

	private void createNative(byte[] toneSound, int samples) {
		if (audioTrack != null) {
			audioTrack.write(toneSound, 0, toneSound.length);
			audioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
			// audioTrack.setNotificationMarkerPosition(samples - 1);
			initialized = true;
		}
	}

	private void play() {
		if (audioTrack != null && initialized) {
			audioTrack.stop();
			audioTrack.reloadStaticData();
			audioTrack.play();
		}
	}

	public void stop() {
		try {
			if (initialized && audioTrack != null)
				audioTrack.stop();
		} catch (Exception ex) {
			Logger.get().log("Fail stopping track ", ex);
		}
	}

}