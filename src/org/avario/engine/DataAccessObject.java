package org.avario.engine;

import org.avario.engine.prefs.Preferences;
import org.avario.engine.sensors.GpsMovement;
import org.avario.engine.sensors.MovementFactor;
import org.avario.utils.Logger;

import android.location.Location;
import android.os.SystemClock;

public class DataAccessObject {
	protected static DataAccessObject THIS;
	protected volatile Location lastThermal;
	protected volatile Location lastlocation;

	protected volatile long lastlocationTimestamp = 0;

	protected volatile float bearing = 0f;
	// protected boolean baroFix = false;
	protected volatile float lastAltitude = -1;

	private volatile float heading = -1f;
	private volatile float windDirectionBearing = -1f;

	private volatile float maxSpeed = 0f;

	private MovementFactor movementFactor = new GpsMovement();
	protected ThermalingTask thermalTask = new ThermalingTask();
	protected HeadingTask headingTask = new HeadingTask();

	protected DataAccessObject() {
	}

	public static void init() {
		THIS = new DataAccessObject();
		THIS.thermalTask.start();
		THIS.headingTask.start();
	}

	public static void clear() {
		THIS.thermalTask.stop();
		THIS.headingTask.stop();
	}

	public static DataAccessObject get() {
		return THIS;
	}

	public void setMovementFactor(MovementFactor factor) {
		this.movementFactor = factor;
	}

	public MovementFactor getMovementFactor() {
		return movementFactor;
	}

	public Location getLastlocation() {
		return lastlocation;
	}

	public Location getLastThermal() {
		return lastThermal;
	}

	public float getLastVSpeed() {
		// float speed = 0f;
		return movementFactor.getValue();
		// if (baroFix) {
		// speed = vSpeedRegression.getSlope();
		// } else if (isGPSFix()) {
		// speed = vSpeedRegression.getLastDelta();
		// }
		// return speed * 1000.0f;
	}

	public void resetVSpeed() {
		movementFactor.reset();
		LocationsHistory.get().clearLocations();
	}

	public void setLastlocation(Location nowlocation) {
		lastlocation = nowlocation;
		if (lastAltitude > 0) {
			// -- set the altitude from the barometer
			lastlocation.setAltitude(lastAltitude);
		}
		lastlocationTimestamp = SystemClock.elapsedRealtime();
		makeWind(lastlocation);
	}

	public float getWindDirectionBearing() {
		return windDirectionBearing;
	}

	public float getLastAltitude() {
		if (lastAltitude < 0) {
			if (lastlocation != null) {
				return (float) lastlocation.getAltitude();
			}
		}
		return lastAltitude;
	}

	public synchronized void setLastAltitude(float lastAltitude) {
		this.lastAltitude = lastAltitude;
	}

	// public synchronized void setBaroLastAltitude(float lastAltitude) {
	// this.lastAltitude = lastAltitude;
	// movementFactor.notify(System.nanoTime() / 1000000d, lastAltitude);
	// }

	// public synchronized void setGpsLastAltitude(float lastAltitude) {
	// this.lastAltitude = lastAltitude;
	// movementFactor.notify(System.nanoTime() / 1000000d, lastAltitude);
	// }

	public float getBearing() {
		return bearing;
	}

	public float getHeading() {
		return heading;
	}

	public void setBearing(float bearing) {
		this.bearing = bearing;
	}

	public boolean isGPSFix() {
		return ((SystemClock.elapsedRealtime() - lastlocationTimestamp) < 5000);
	}

	protected void makeWind(Location location) {
		if (location != null && location.hasSpeed()) {
			float speed = location.getSpeed();
			if (maxSpeed < speed) {
				maxSpeed = speed;
				windDirectionBearing = (180 + location.getBearing()) % 360;
				Logger.get().log("wind direction bearing at" + windDirectionBearing + " on speed " + maxSpeed);
			}
		}
	}

	private class ThermalingTask implements Runnable {
		private volatile Thread blinker;
		private Thread thr;

		private ThermalingTask() {
			thr = new Thread(this);
		}

		public void start() {
			thr.start();
		}

		public void stop() {
			blinker = null;
		}

		@Override
		public void run() {
			try {
				Logger.get().log(
						"Start thermal interval: " + Preferences.min_thermal_interval + "; min gain: "
								+ Preferences.min_thermal_gain);
				blinker = Thread.currentThread();
				while (blinker == Thread.currentThread()) {
					float startAltitude = getLastAltitude();
					Thread.sleep(Math.round(Preferences.min_thermal_interval));
					float endAltitude = getLastAltitude();
					if (startAltitude > 0 && (startAltitude + Preferences.min_thermal_gain <= endAltitude)) {
						lastThermal = lastlocation;
					}
				}
			} catch (Exception e) {
				Logger.get().log("Thermal task exception ", e);
			} finally {
				Logger.get().log("Thermal task terminated");
			}
		}
	}

	private class HeadingTask implements Runnable {
		private volatile Thread blinker;
		private Thread thr;
		private Location headingReferrence;

		private HeadingTask() {
			thr = new Thread(this);
		}

		public void start() {
			thr.start();
		}

		public void stop() {
			blinker = null;
		}

		@Override
		public void run() {
			try {
				blinker = Thread.currentThread();
				while (blinker == Thread.currentThread()) {
					if (headingReferrence != null && lastlocation != null
							&& headingReferrence.getTime() < lastlocation.getTime()) {
						heading = headingReferrence.bearingTo(lastlocation);
					}
					headingReferrence = lastlocation;
					Thread.sleep(3000);
				}
			} catch (InterruptedException e) {
			} finally {
				Logger.get().log("Heading task terminated");
			}
		}
	}

}
