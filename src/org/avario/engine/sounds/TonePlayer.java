package org.avario.engine.sounds;

import org.avario.utils.Logger;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class TonePlayer {
	private final float duration = 0.400f; // seconds
	private final int sampleRate = 8000;
	private final int numSamples = (int) (duration * sampleRate);
	private AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_DTMF, sampleRate,
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
			play(toneSound, numSamples);
		} catch (Exception ex) {
			Logger.get().log("Fail playing track ", ex);
		}
	}

	private void play(byte[] toneSound, int samples) {
		if (audioTrack != null) {

			switch (audioTrack.getPlayState()) {
			case AudioTrack.PLAYSTATE_PAUSED:
			case AudioTrack.PLAYSTATE_PLAYING:
				audioTrack.stop();
				break;
			}

			audioTrack.reloadStaticData();
			audioTrack.setPlaybackHeadPosition(0);
			audioTrack.write(toneSound, 0, toneSound.length);
			audioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
			// audioTrack.setNotificationMarkerPosition(samples - 1);
			audioTrack.play();
		}
	}

	public void stop() {
		try {
			if (audioTrack != null) {
				switch (audioTrack.getPlayState()) {
				case AudioTrack.PLAYSTATE_PAUSED:
				case AudioTrack.PLAYSTATE_PLAYING:
					audioTrack.stop();
					break;
				}
			}
		} catch (Exception ex) {
			Logger.get().log("Fail stopping track ", ex);
		}
	}

}