package org.avario.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.avario.engine.prefs.Preferences;

public class UnitsConverter {

	public static double msTokmh(double msSpeed) {
		if (Double.isNaN(msSpeed) || Double.isInfinite(msSpeed)) {
			return 0.0;
		}
		return msSpeed * 3.6d;
	}

	public static float msTokmh(float msSpeed) {
		if (Float.isNaN(msSpeed) || Float.isInfinite(msSpeed)) {
			return 0f;
		}
		return msSpeed * 3.6f;
	}

	public static String timeSpan(long since) {
		long diffInSeconds = (System.currentTimeMillis() - since) / 1000;
		long diff[] = new long[] { 0, 0, 0 };
		/* sec */diff[2] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
		/* min */diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
		/* hours */diff[0] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
		return String.format("%02d:%02d:%02d", diff[0], diff[1], diff[2]);
	}

	public static String humanTimeSpan(long diffInMSeconds) {
		long diffInSeconds = diffInMSeconds / 1000;
		long diff[] = new long[] { 0, 0, 0 };
		/* sec */diff[2] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
		/* min */diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
		/* hours */diff[0] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
		String humanString = String.format("%02d:%02d:%02d", diff[0], diff[1], diff[2]);
		return humanString;
	}

	public static float verticalSpeed(float lastAltitudeValue, long lastAltitudeTimestamp, float newAltitude,
			long newAltitudeTimestamp) {
		if (newAltitudeTimestamp == lastAltitudeTimestamp) {
			return 0f;
		}
		float timeSpan = (newAltitudeTimestamp - lastAltitudeTimestamp) / 1000f;
		float distSpan = (newAltitude - lastAltitudeValue);
		float ret = (distSpan == 0f) ? 0f : (distSpan / timeSpan);
		if (Float.isNaN(ret) || Float.isInfinite(ret)) {
			return 0f;
		}
		return ret;
	}

	public static String humanTime(long timeInMillis) {
		Date date = new Date(timeInMillis);
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		return sdf.format(date);
	}

	public static float m2feets(float meters) {
		if (Float.isNaN(meters) || Float.isInfinite(meters)) {
			return 0f;
		}
		return 3.280839895f * meters;
	}

	public static float feets2m(float feets) {
		if (Float.isNaN(feets) || Float.isInfinite(feets)) {
			return 0f;
		}
		return feets / 3.280839895f;
	}

	public static float km2miles(float km) {
		if (Float.isNaN(km) || Float.isInfinite(km)) {
			return 0f;
		}
		return 0.621371f * km;
	}

	public static float mps2fpm(float speed) {
		if (Float.isNaN(speed) || Float.isInfinite(speed)) {
			return 0f;
		}
		return 196.850394f * speed;
	}

	public static float toPreferredVSpeed(float speed) {
		if (Float.isNaN(speed) || Float.isInfinite(speed)) {
			return 0f;
		}
		float ret = speed;
		switch (Preferences.units_system) {
		case 1: // Metric
			break;
		case 2: // Imperial
			ret = mps2fpm(speed);
			break;
		}
		return ret;
	}

	public static float toPreferredLong(float km) {
		if (Float.isNaN(km) || Float.isInfinite(km)) {
			return 0f;
		}
		float ret = km;
		switch (Preferences.units_system) {
		case 1: // Metric
			break;
		case 2: // Imperial
			ret = km2miles(km);
			break;
		}
		return ret;
	}

	public static float toPreferredShort(float meters) {
		if (Float.isNaN(meters) || Float.isInfinite(meters)) {
			return 0f;
		}
		float ret = meters;
		switch (Preferences.units_system) {
		case 1: // Metric
			break;
		case 2: // Imperial
			ret = m2feets(meters);
			break;
		}
		return ret;
	}

	public static float fromPreferredShort(float preferred) {
		if (Float.isNaN(preferred) || Float.isInfinite(preferred)) {
			return 0f;
		}
		float ret = preferred;
		switch (Preferences.units_system) {
		case 1: // Metric
			break;
		case 2: // Imperial
			ret = m2feets(preferred);
			break;
		}
		return ret;
	}

	public static String preferredDistLong() {
		String ret = " km";
		switch (Preferences.units_system) {
		case 1: // Metric
			break;
		case 2: // Imperial
			ret = " miles";
			break;
		}
		return ret;
	}

	public static String preferredDistShort() {
		String ret = " m";
		switch (Preferences.units_system) {
		case 1: // Metric
			break;
		case 2: // Imperial
			ret = " F";
			break;
		}
		return ret;
	}

	public static String normalizedDistance(float distance) {
		if (Float.isNaN(distance)) {
			return " unknown";
		}
		String tooLong = Preferences.units_system == 1 ? ">500km" : ">300miles";
		String distStr = distance > 500000 ? tooLong : StringFormatter.noDecimals(UnitsConverter
				.toPreferredShort(distance)) + UnitsConverter.preferredDistShort();
		if (distance < 500000 && distance > 5000f) {
			distStr = StringFormatter.noDecimals(UnitsConverter.toPreferredLong(distance / 1000f))
					+ UnitsConverter.preferredDistLong();
		}
		return distStr;
	}
}
