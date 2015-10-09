package org.avario.engine.datastore;

import java.util.StringTokenizer;

import org.avario.engine.prefs.Preferences;
import org.avario.engine.sensors.GpsMovement;
import org.avario.engine.sensors.MovementFactor;
import org.avario.engine.wind.WindCalculator;
import org.avario.utils.Logger;

import android.location.Location;

public class DataAccessObject {
	protected static DataAccessObject THIS;

	protected Location lastlocation;
	protected long lastSystemFix;

	protected float bearing = 0f;
	protected float lastAltitude = -1;
	protected float lastPresure = -1;
	protected volatile float lastVSpeed = 0;
	protected float refAltitude = -1;
	protected double geoidHeight = 0f;

	private double windDirectionBearing = -1d;
	private double gForce = 1f;
	private float temperature = 0f;
	private float gpsAltitude = 0f;

	private MovementFactor movementFactor = new GpsMovement();
	private FlightStatusTask flightTask = new FlightStatusTask();
	private ThermalingTask thermalTask = new ThermalingTask();
	private HeadingTask headingTask = new HeadingTask();
	private AltitudeGainTask altitudeGainTask = new AltitudeGainTask();
	private WindCalculator windCalculator = new WindCalculator(16, 0.3, 300);
	private NotificationTask sensorNotification = new NotificationTask();

	protected DataAccessObject() {
	}

	public static void init() {
		THIS = new DataAccessObject();
		THIS.thermalTask.start();
		THIS.headingTask.start();
		THIS.altitudeGainTask.start();
		THIS.flightTask.start();
		// THIS.sensorNotification.start();
	}

	public static void clear() {
		THIS.thermalTask.stop();
		THIS.headingTask.stop();
		THIS.altitudeGainTask.stop();
		THIS.flightTask.stop();
		// THIS.sensorNotification.stop();
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
		return lastVSpeed;
	}

	public synchronized void upadteVSpeed() {
		lastVSpeed = movementFactor.getValue();
	}

	public synchronized void resetVSpeed() {
		movementFactor.reset();
		altitudeGainTask.reset();
	}

	public void setLastlocation(Location nowlocation) {
		lastlocation = nowlocation;
		if (lastlocation != null) {
			lastSystemFix = System.currentTimeMillis();
			gpsAltitude = nowlocation.hasAltitude() ? (float) nowlocation.getAltitude() : gpsAltitude;
		}
		if (lastAltitude > 0) {
			lastlocation.setAltitude(lastAltitude);
		}
		makeWind(lastlocation);
		makeQFE(lastlocation);
	}

	public float getGPSAltitude() {
		return gpsAltitude;
	}

	public boolean isInFlight() {
		return flightTask.isInFlight();
	}

	public double getWindDirectionBearing() {
		return windDirectionBearing;
	}

	public float getLastAltitude() {
		if (lastAltitude < 0 && lastlocation != null) {
			return (float) lastlocation.getAltitude();
		}
		return lastAltitude;
	}

	public void setLastAltitude(float lastAltitude) {
		this.lastAltitude = lastAltitude;
	}

	public float getBearing() {
		return bearing;// lastlocation != null && lastlocation.hasBearing() ?
						// lastlocation.getBearing() : bearing;
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

	public float getSensitivity() {
		float dec = Preferences.baro_sensitivity / 10f;
		float dinamicSensivity = (float) (Preferences.baro_sensitivity - Math.min(Preferences.baro_sensitivity / 2f,
				gForce * dec));
		return Math.abs(dinamicSensivity);
	}

	public synchronized boolean isGPSFix() {
		return (System.currentTimeMillis() - lastSystemFix) < 3000;
	}

	protected void makeWind(Location location) {
		if (location != null && location.hasSpeed() && location.hasBearing()) {
			windCalculator.addSpeedVector(location.getBearing(), location.getSpeed(), location.getTime() / 1000.0);
			windDirectionBearing = windCalculator.getWindDirection();
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
		return geoidHeight;
	}

	public void setNmeaGGA(String nmeaGGA) {
		if (nmeaGGA != null && geoidHeight == 0) {
			try {
				StringTokenizer st = new StringTokenizer(nmeaGGA, ",");
				String prevValue = "0";
				boolean firstM = true;
				while (st.hasMoreTokens()) {
					String currentValue = st.nextToken();
					if (currentValue.equals("M")) {
						if (firstM) { // First M is the altitude from NMEA
							Logger.get().log("Nmea altitude: " + prevValue);
							firstM = false;
						} else if (prevValue != null && !"".equals(prevValue)) {
							Logger.get().log("Nmea geoid height: " + prevValue);
							geoidHeight = Double.parseDouble(prevValue);
							break;
						}
					}
					prevValue = currentValue;
				}
			} catch (Exception ex) {
				// Logger.get().log("Invalid GGA " + nmeaGGA, ex);
			}
		}
	}

	public float getLastPresure() {
		return lastPresure;
	}

	public void setLastPresure(float lastPresure) {
		this.lastPresure = lastPresure;
	}

	public double getGForce() {
		return gForce;
	}

	public void setGForce(double gForce) {
		this.gForce = gForce;
	}

	public float getNps() {
		return sensorNotification.getNps();
	}

}
