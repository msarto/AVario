package org.avario.engine.sounds;

import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.AccelerometerConsumer;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.Speaker;
import org.avario.utils.StringFormatter;
import org.avario.utils.UnitsConverter;

public class BeepBeeper implements Runnable {

	private volatile boolean doBeep = false;
	private static BeepBeeper THIS;
	private final AccelerationBeep accelerationBeep = new AccelerationBeep();
	private volatile boolean isLift = false;

	protected BeepBeeper() {
	}

	public static void init() {
		clear();
		THIS = new BeepBeeper();
		THIS.start();
	}

	public static void clear() {
		if (THIS != null) {
			THIS.stop();
		}
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
					double sensitivity = DataAccessObject.get().getSensitivity();

					if (!validateThisSpeed(beepSpeed)) {
						isLift = false;
						Thread.sleep(Math.round(5f * sensitivity));
					} else {
						boolean bAlarm = beepSpeed < Math.min(Preferences.sink_start, Preferences.sink_alarm);
						if (bAlarm) {
							ToneProducer.get().getAlarmTone().beep();
						}

						if (beepSpeed > 0) {
							isLift = true;
							AsyncTone liftBeep = ToneProducer.get().getLiftTone();
							liftBeep.setSpeed(beepSpeed);
							liftBeep.beep();
						} else if (beepSpeed < 0) {
							isLift = false;
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
		} catch (Throwable ex) {
			Logger.get().log("Fail init beep: ", ex);
		}

	}

	private boolean validateThisSpeed(float beepSpeed) {
		if (Float.isNaN(beepSpeed)) {
			return false;
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

		if (Preferences.sound_inflight && !DataAccessObject.get().isInFlight()) {
			return false;
		}

		return true;
	}

	private void start() {
		doBeep = true;
		new Thread(this).start();
		//-- SensorProducer.get().registerConsumer(accelerationBeep);
	}

	public void stop() {
		SensorProducer.get().registerConsumer(accelerationBeep);
		ToneProducer.get().getLiftTone().stop();
		ToneProducer.get().getSyncTone().stop();
		ToneProducer.get().getPrenotifyTone().stop();
		doBeep = false;
	}

	private class AccelerationBeep implements AccelerometerConsumer {
		long lastAcceleration = System.currentTimeMillis();

		@Override
		public void notifyAcceleration(float x, float y, float z) {
			if (Preferences.sound_inflight && !DataAccessObject.get().isInFlight()) {
				return;
			}

			if (Preferences.prenotify_interval > 0 && !isLift
					&& System.currentTimeMillis() - lastAcceleration > Preferences.prenotify_interval) {
				double gF = Math.sqrt(x * x + y * y + z * z);
				if (gF < (12f - (Preferences.baro_sensitivity * 0.1f))) {
					ToneProducer.get().getPrenotifyTone().beep();
					lastAcceleration = System.currentTimeMillis();
				}
			}
		}

	}
}
