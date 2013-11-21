package org.avario.engine;

import org.avario.engine.prefs.Preferences;
import org.avario.utils.LinearRegression;
import org.avario.utils.Logger;

import android.location.Location;
import android.os.SystemClock;

public class DataAccessObject {
	protected static DataAccessObject THIS;
	protected volatile Location lastThermal;
	protected volatile Location lastlocation;
	protected volatile double gpsaltitude;

	protected volatile long lastlocationTimestamp = 0;

	protected volatile float bearing = 0f;
	protected boolean baroFix = false;
	protected volatile float lastAltitude = -1;

	private volatile float heading = -1f;
	private volatile float windDirectionBearing = -1f;

	private volatile float maxSpeed = 0f;

	private LinearRegression vSpeedRegression = new LinearRegression();
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

	public Location getLastlocation() {
		return lastlocation;
	}

	public Location getLastThermal() {
		return lastThermal;
	}

	public float getLastVSpeed() {
		return (baroFix ? vSpeedRegression.getSlope() : vSpeedRegression.getLastDelta()) * 1000.0f;
	}

	public void resetVSpeed() {
		vSpeedRegression.reset();
		LocationsHistory.get().clearLocations();
	}

	public void setLastlocation(Location nowlocation) {
		lastlocation = nowlocation;
		gpsaltitude = nowlocation.hasAltitude() ? nowlocation.getAltitude() : gpsaltitude;
		if (baroFix && lastAltitude > 0) {
			// -- set the altitude from the barometrer
			lastlocation.setAltitude(lastAltitude);
		}
		lastlocationTimestamp = SystemClock.elapsedRealtime();
		makeWind(lastlocation);
	}

	public float getWindDirectionBearing() {
		return windDirectionBearing;
	}

	public double getGpsAltitude() {
		return gpsaltitude;
	}

	public float getLastAltitude() {
		if (lastAltitude < 0) {
			if (lastlocation != null) {
				return (float) lastlocation.getAltitude();
			}
		}
		return lastAltitude;
	}

	public synchronized void setBaroLastAltitude(float lastAltitude) {
		baroFix = true;
		this.lastAltitude = lastAltitude;
		vSpeedRegression.addSample(System.nanoTime() / 1000000d, lastAltitude, baroFix);
	}

	public synchronized void setGpsLastAltitude(float lastAltitude) {
		if (!baroFix) {
			this.lastAltitude = lastAltitude;
			vSpeedRegression.addSample(System.nanoTime() / 1000000d, lastAltitude, baroFix);
		}
	}

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
