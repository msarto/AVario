package org.avario.engine.sounds;

import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.tone.LiftTone;
import org.avario.engine.sounds.tone.SinkTone;
import org.avario.engine.sounds.wav.WavAlarmTone;
import org.avario.engine.sounds.wav.WavLiftTone;
import org.avario.engine.sounds.wav.WavPrenotifyTone;
import org.avario.engine.sounds.wav.WavSinkTone;

public class ToneProducer {
	protected static final ToneProducer THIS = new ToneProducer();

	protected final AsyncTone[] dinamicTones = new AsyncTone[] { new WavLiftTone(), new WavSinkTone(),
			new WavPrenotifyTone(), new WavAlarmTone() };
	protected final AsyncTone[] staticTones = new AsyncTone[] { new LiftTone(), new SinkTone(), new WavPrenotifyTone(),
			new WavAlarmTone() };

	protected ToneProducer() {

	}

	public static ToneProducer get() {
		return THIS;
	}

	public AsyncTone getLiftTone() {
		switch (Preferences.sound_type) {
		case 1:
			return staticTones[0];
		default:
			return dinamicTones[0];
		}
	}

	public AsyncTone getSyncTone() {
		switch (Preferences.sound_type) {
		case 1:
			return staticTones[1];
		default:
			return dinamicTones[1];
		}

	}

	public AsyncTone getAlarmTone() {
		return dinamicTones[3];
	}

	public AsyncTone getPrenotifyTone() {
		switch (Preferences.sound_type) {
		case 1:
			return staticTones[2];
		default:
			return dinamicTones[2];
		}

	}
}
