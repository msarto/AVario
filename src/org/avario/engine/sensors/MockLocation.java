package org.avario.engine.sensors;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

public abstract class MockLocation implements Runnable {

	public static final List<Double[]> points = new ArrayList<Double[]>();
	static {
		points.add(new Double[] { 23.572648, 46.450058, 222d });
		points.add(new Double[] { 23.572648, 46.450058, 222d });
		points.add(new Double[] { 23.572648, 46.450058, 222d });
		points.add(new Double[] { 23.572648, 46.450058, 222d });
		points.add(new Double[] { 23.572648, 46.450058, 222d });
		points.add(new Double[] { 23.572648, 46.450058, 222d });

		points.add(new Double[] { 23.572648, 46.450058, 222d });
		points.add(new Double[] { 23.572648, 46.450258, 224d });
		points.add(new Double[] { 23.572648, 46.450458, 226d });
		points.add(new Double[] { 23.572648, 46.450658, 225d });
		points.add(new Double[] { 23.572648, 46.450858, 224d });
		points.add(new Double[] { 23.572648, 46.451058, 222d });
		points.add(new Double[] { 23.572648, 46.451258, 221d });
		points.add(new Double[] { 23.572648, 46.451458, 221d });
		points.add(new Double[] { 23.572648, 46.451658, 221d });
		points.add(new Double[] { 23.572648, 46.451858, 221d });
		points.add(new Double[] { 23.572648, 46.452058, 221d });
		points.add(new Double[] { 23.572648, 46.452258, 221d });
	}

	public abstract void notifyLocation(Location location);

	public void start() {
		new Thread(this).start();
	}

	public void run() {
		try {
			Thread.sleep(5000);
			for (Double[] point : points) {
				Location location = new Location("mock");
				location.setAccuracy(1);
				location.setLongitude(point[0]);
				location.setLatitude(point[1]);
				location.setAltitude(point[2]);
				location.setSpeed(Math.round(Math.random() * 20));
				location.setTime(System.currentTimeMillis());
				notifyLocation(location);

				Thread.sleep(1000);

			}
		} catch (InterruptedException e) {

		}
	}
}
