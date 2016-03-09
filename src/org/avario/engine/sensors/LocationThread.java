package org.avario.engine.sensors;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.SensorProducer;
import org.avario.engine.SensorThread;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.utils.Logger;

import android.content.Context;
import android.hardware.SensorEvent;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class LocationThread extends SensorThread<Location> implements LocationListener, NmeaListener {

	private final LocationManager locationManager;

	public LocationThread() {
		locationManager = (LocationManager) AVarioActivity.CONTEXT.getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void run() {
		locationManager.removeUpdates(LocationThread.this);
		AVarioActivity.CONTEXT.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LocationThread.this);
					locationManager.addNmeaListener(LocationThread.this);
				} catch (Exception e) {
					Toast.makeText(AVarioActivity.CONTEXT,
							AVarioActivity.CONTEXT.getApplicationContext().getString(R.string.gps_fail),
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public void stop() {
		locationManager.removeUpdates(LocationThread.this);
		locationManager.removeNmeaListener(LocationThread.this);
	}

	@Override
	public synchronized void onLocationChanged(final Location newLocation) {
		callbackThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (newLocation.hasAltitude()
							&& (DataAccessObject.get().getMovementFactor() instanceof GpsMovement)) {
						float nowAltitude = (float) (newLocation.getAltitude() - DataAccessObject.get()
								.getGeoidHeight());
						DataAccessObject.get().getMovementFactor().notify(System.nanoTime() / 1000000d, nowAltitude);
						DataAccessObject.get().setLastAltitude(nowAltitude);
					}
					DataAccessObject.get().setLastlocation(newLocation);
					SensorProducer.get().notifyGpsConsumers(newLocation);
				} catch (Throwable ex) {
					Logger.get().log("Fail to notify location changes ", ex);
				}
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

	@Override
	public void onNmeaReceived(long ts, final String nmea) {
		// $GPGGA,102010.0,4646.486229,N,02336.101507,E,1,07,0.6,391.7,M,37.0,M,,*52
		if (nmea != null && nmea.startsWith("$GPGGA,") && !nmea.startsWith("$GPGGA,,,")) {
			DataAccessObject.get().setNmeaGGA(nmea);
		}
	}
}
