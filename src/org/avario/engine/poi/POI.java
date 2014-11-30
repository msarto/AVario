package org.avario.engine.poi;

import android.location.Location;

public class POI {

	private int version;
	private String name;
	private double latitude;
	private double longitude;
	private double altitude = 0;

	public POI(int version) {
		this.version = version;
	}

	public POI(String name, int version) {
		this.name = name;
		this.version = version;
	}

	public POI(String name, double latitude, double longitude, double altitude,
			int version) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.version = version;
		this.altitude = altitude;
	}

	public POI(String name, Location location, int version) {
		this.name = name;
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
		this.altitude = location.getAltitude();
		this.version = version;
	}

	public float bearingTo(Location location) {
		if (location == null) {
			return 0;
		}
		Location thisLocation = new Location(name);
		thisLocation.setLatitude(latitude);
		thisLocation.setLongitude(longitude);
		return location.bearingTo(thisLocation);
	}

	public float distanceTo(Location location) {
		if (location == null) {
			return Float.NaN;
		}
		Location thisLocation = new Location(name);
		thisLocation.setLatitude(latitude);
		thisLocation.setLongitude(longitude);
		return location.distanceTo(thisLocation);
	}

	public String getName() {
		return name;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String toString() {
		return "name: " + name + "; lat: " + latitude + "; lng: " + longitude;
	}

	public int getVersion() {
		return version;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
}
