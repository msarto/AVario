package org.avario.engine.sensors;

import org.avario.engine.DataAccessObject;
import org.avario.engine.SensorProducer;
import org.avario.engine.SensorThread;
import org.avario.utils.Logger;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorEvent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationThread extends SensorThread<Location> implements LocationListener {

	private final LocationManager locationManager;

	public LocationThread(Activity activity) {
		super(activity);
		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void run() {
		try {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LocationThread.this);
				}
			});
			locationManager.removeUpdates(LocationThread.this);
		} catch (Exception e) {
			Logger.get().log("GPS initialization fail ", e);
		}
	}

	@Override
	public void stop() {
		locationManager.removeUpdates(LocationThread.this);
	}

	@Override
	public synchronized void onLocationChanged(Location newLocation) {
		DataAccessObject.get().setLastlocation(newLocation);
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				SensorProducer.get().notifyGpsConsumers(DataAccessObject.get().getLastlocation());
			}
		});
	}

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// GPS signal is back. Cool
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// public static final int OUT_OF_SERVICE = 0;
		// public static final int TEMPORARILY_UNAVAILABLE = 1;
		// public static final int AVAILABLE = 2;
	}

	@Override
	protected void notifySensorChanged(SensorEvent sensorEvent) {
		// TODO Auto-generated method stub
	}

}
