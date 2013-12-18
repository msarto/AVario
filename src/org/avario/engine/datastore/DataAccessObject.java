package org.avario.engine.datastore;

import org.avario.engine.sensors.GpsMovement;
import org.avario.engine.sensors.MovementFactor;
import org.avario.utils.Logger;

import android.location.Location;
import android.os.SystemClock;

public class DataAccessObject {
	protected static DataAccessObject THIS;
	protected volatile Location lastlocation;

	protected volatile long lastlocationTimestamp = 0;

	protected volatile float bearing = 0f;
	protected volatile float lastAltitude = -1;

	private volatile float windDirectionBearing = -1f;
	private volatile float temperature = 0f;

	private volatile float maxSpeed = 0f;

	private MovementFactor movementFactor = new GpsMovement();
	protected ThermalingTask thermalTask = new ThermalingTask();
	protected HeadingTask headingTask = new HeadingTask();
	protected AltitudeGainTask altitudeGainTask = new AltitudeGainTask();

	protected DataAccessObject() {
	}

	public static void init() {
		THIS = new DataAccessObject();
		THIS.thermalTask.start();
		THIS.headingTask.start();
		THIS.altitudeGainTask.start();
	}

	public static void clear() {
		THIS.thermalTask.stop();
		THIS.headingTask.stop();
		THIS.altitudeGainTask.stop();
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
		return thermalTask.getLastThermal();
	}

	public float getLastVSpeed() {
		return movementFactor.getValue();
	}

	public void resetVSpeed() {
		movementFactor.reset();
		altitudeGainTask.reset();
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

	public float getBearing() {
		return bearing;
	}

	public float getHeading() {
		return headingTask.getHeading();
	}

	public void setBearing(float bearing) {
		this.bearing = bearing;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
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

	public double getHistoryAltimeterGain() {
		return altitudeGainTask.getAltitudeGain();
	}
}
