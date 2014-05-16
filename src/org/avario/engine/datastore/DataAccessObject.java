package org.avario.engine.datastore;

import java.util.StringTokenizer;

import org.avario.engine.sensors.GpsMovement;
import org.avario.engine.sensors.MovementFactor;
import org.avario.utils.Logger;

import android.location.Location;

public class DataAccessObject {
	protected static DataAccessObject THIS;

	protected volatile Location lastlocation;

	protected volatile float bearing = 0f;
	protected volatile float lastAltitude = -1;
	protected volatile float refAltitude = -1;
	protected volatile String nmeaGGA;

	private volatile float windDirectionBearing = -1f;
	private volatile float temperature = 0f;
	private volatile float maxSpeed = 0f;

	private MovementFactor movementFactor = new GpsMovement();
	private ThermalingTask thermalTask = new ThermalingTask();
	private HeadingTask headingTask = new HeadingTask();
	private AltitudeGainTask altitudeGainTask = new AltitudeGainTask();

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
			lastlocation.setAltitude(lastAltitude);
		}
		makeWind(lastlocation);
		makeQFE(lastlocation);
	}

	public float getWindDirectionBearing() {
		return windDirectionBearing;
	}

	public float getLastAltitude() {
		if (lastAltitude < 0 && lastlocation != null) {
			return (float) lastlocation.getAltitude();
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
		return (lastlocation != null && (System.currentTimeMillis() - lastlocation.getTime()) < 5000);
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

	protected void makeQFE(Location location) {
		if (refAltitude < 0 && location != null && location.hasSpeed()) {
			float speed = location.getSpeed();
			if (speed > 4) {
				refAltitude = getLastAltitude();
				Logger.get().log("Take of altitude " + refAltitude);
			}
		}
	}

	public float getQFE() {
		return refAltitude < 0 ? 0f : (getLastAltitude() - refAltitude);
	}

	public double getHistoryAltimeterGain() {
		return altitudeGainTask.getAltitudeGain();
	}

	public double getGeoidHeight() {
		// $GPGGA,102010.0,4646.486229,N,02336.101507,E,1,07,0.6,391.7,M,37.0,M,,*52
		double geoidHeight = 0f;
		try {
			if (nmeaGGA != null) {
				StringTokenizer st = new StringTokenizer(nmeaGGA, ",");
				String prevValue = "0";
				boolean firstM = true;
				while (st.hasMoreTokens()) {
					String currentValue = st.nextToken();
					if (currentValue.equals("M")) {
						if (firstM) { // First M is the altitude from NMEA
							Logger.get().log("Nmea altitude: " + prevValue);
							firstM = false;
						} else {
							Logger.get().log("Nmea geoid height: " + prevValue);
							geoidHeight = Double.valueOf(prevValue);
							break;
						}
					}
					prevValue = currentValue;
				}
			}
		} catch (Exception ex) {
			Logger.get().log("Fail computing geoid H ", ex);
		}
		return geoidHeight;
	}

	public void setNmeaGGA(String nmeaGGA) {
		this.nmeaGGA = nmeaGGA;
	}
}
