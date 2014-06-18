package org.avario.engine.sounds;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.Speaker;
import org.avario.utils.StringFormatter;
import org.avario.utils.UnitsConverter;

public class BeepBeeper implements Runnable {

	private volatile boolean doBeep = false;
	private static BeepBeeper THIS;

	protected BeepBeeper() {
	}

	public static void init() {
		THIS = new BeepBeeper();
		THIS.start();
	}

	public static void clear() {
		THIS.stop();
	}

	@Override
	public void run() {
		try {
			while (doBeep) {
				try {
					DataAccessObject.get().upadteVSpeed();
					float beepSpeed = DataAccessObject.get().getLastVSpeed();
					beepSpeed = beepSpeed > 5 ? 5 : beepSpeed;
					beepSpeed = beepSpeed < -5 ? -5 : beepSpeed;

					if (!validateThisSpeed(beepSpeed)) {
						Thread.sleep(200);
					} else {
						if (beepSpeed > 0) {
							AsyncTone liftBeep = ToneProducer.get().getLiftTone();
							liftBeep.setSpeed(beepSpeed);
							liftBeep.beep();
						} else if (beepSpeed < 0) {
							AsyncTone sinkBeep = ToneProducer.get().getSyncTone();
							sinkBeep.setSpeed(beepSpeed);
							sinkBeep.beep();
						}						
						if (Preferences.use_speach) {
							float saySpeed = UnitsConverter.toPreferredVSpeed(beepSpeed);
							Speaker.get().say(
									Preferences.units_system == 2 ? StringFormatter.noDecimals(saySpeed)
											: StringFormatter.oneDecimal(saySpeed));
						}						
						 Thread.sleep(Math.round(100 - 20 * beepSpeed));
					}
				} catch (InterruptedException e) {
					break;
				} catch (Exception ex) {
					Logger.get().log("Fail in beep: ", ex);
				} 
			}
		} catch (Exception ex) {
			Logger.get().log("Fail init beep: ", ex);
		}

	}

	private boolean validateThisSpeed(float beepSpeed) {
		if (beepSpeed == Float.NaN) {
			return false;
		}

		if ((beepSpeed > -Preferences.lift_start) && (beepSpeed < Preferences.lift_start)) {
			prenotifyThermal();
		}

		if (Math.abs(beepSpeed) < Preferences.lift_start) {
			return false;
		}

		if (beepSpeed <= 0f && beepSpeed > Preferences.sink_start) {
			return false;
		}

		if (Math.abs(beepSpeed) > 15) {
			return false;
		}

		return true;
	}

	private void prenotifyThermal() {
		if (Preferences.prenotify_interval > 0 && DataAccessObject.get().isGPSFix()) {
			ToneProducer.get().getPrenotifyTone().beep();
		}
	}

	private void start() {
		doBeep = true;
		new Thread(this).start();
	}

	public void stop() {
		ToneProducer.get().getLiftTone().stop();
		ToneProducer.get().getSyncTone().stop();
		ToneProducer.get().getPrenotifyTone().stop();
		doBeep = false;
	}
}
