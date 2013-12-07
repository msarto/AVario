package org.avario.utils;

import org.avario.R;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

public class Speaker {
	protected static Speaker THIS;
	private TextToSpeech talker;
	private boolean available = true;

	protected Speaker(Context context) {
		try {
			this.talker = new TextToSpeech(context, null);
		} catch (Exception ex) {
			available = false;
			Logger.get().log("Speach is not available", ex);
			Toast.makeText(context, R.string.speaker_fail, Toast.LENGTH_LONG).show();
		}
	}

	public static void init(Context context) {
		THIS = new Speaker(context);
	}

	public static Speaker get() {
		return THIS;
	}

	public synchronized void say(String text2say) {

		if (!talker.isSpeaking() && available) {
			if (text2say != null && !text2say.equals("") && !text2say.toLowerCase().equals("nan"))
				talker.speak(text2say, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	public static void clear() {
		THIS.close();
	}

	private void close() {
		talker.stop();
		talker.shutdown();
	}
}
