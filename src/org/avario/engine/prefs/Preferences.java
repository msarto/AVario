package org.avario.engine.prefs;

import org.avario.AVarioActivity;
import org.avario.ui.prefs.PreferencesMenu;
import org.avario.utils.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class Preferences {
	public static volatile boolean use_speach = false;
	public static volatile boolean auto_track = false;
	public static volatile boolean use_sensbox = false;

	public static volatile int beep_interval = 500;
	public static volatile float sink_start = -1.5f;
	public static volatile float lift_start = 0.2f;
	public static volatile int tone_variation = 100;
	public static volatile int prenotify_interval = 3000;

	public static volatile int location_history = 5;
	public static volatile int heading_interval = 2000;

	public static volatile int baro_sensitivity = 25;

	public static volatile float compass_filter_sensitivity = 2f;
	public static volatile int max_last_thermal_distance = 200;
	public static volatile float min_thermal_interval = 3000;
	public static volatile float min_thermal_gain = 3;
	public static volatile int units_system = 1; // 1-metric; 2-imperial
	
	public static volatile int lift_hz = 600;
	public static volatile int sink_hz = 350;

	private static Context context;

	private static void checkForUpdateVersion() {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			String version = pInfo.versionName;
			Logger.get().log("Current version " + version);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String prefVersion = prefs.getString("appVersion", "");
			Logger.get().log("pref version " + prefVersion);

			if (!version.equals(prefVersion)) {
				Editor ed = prefs.edit();
				ed.clear();
				ed.putString("appVersion", version);
				ed.commit();
			}
		} catch (Exception e) {
			Logger.get().log("Unknown version...", e);
		}
	}

	private static void setAppSoundVolume(int appVolume) {
		try {
			AudioManager audio = (AudioManager) Preferences.context.getSystemService(Context.AUDIO_SERVICE);
			int mediaVal = Math.round((audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100f) * appVolume);
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, mediaVal, 0);
		} catch (Exception e) {
			Logger.get().log("Unable o set app volume...", e);
		}
	}

	private static void setDefaultBaroSensitivity() {
		try {

			String baroSetting = getString("baro_sensitivity", "-1");
			Logger.get().log("baroSetting: " + baroSetting);

			if (baroSetting.equals("-1")) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				int defaultValue = getDefaultBaroSensitivity();
				Logger.get().log(Build.MODEL + " set default baro sensitivity " + defaultValue);
				Editor ed = prefs.edit();
				// ed.clear();
				ed.putString("baro_sensitivity", String.valueOf(defaultValue));
				ed.commit();
			}
		} catch (Exception e) {
			Logger.get().log("Unable o set app volume...", e);
		}
	}

	private static int getDefaultBaroSensitivity() {
		final String model = Build.MODEL.toLowerCase().trim();
		if (model.equals("GT-I9300".toLowerCase()) || model.startsWith("T999".toLowerCase())
				|| model.equals("I747".toLowerCase())) {
			return 35;
		}
		return 25;
	}

	public static void update(Context context) {
		Preferences.context = context;

		checkForUpdateVersion();
		setDefaultBaroSensitivity();

		use_speach = getBool("use_speach", use_speach);
		auto_track = getBool("auto_track", auto_track);
		use_sensbox = getBool("use_sensbox", use_sensbox);
		
		beep_interval = Math.round(1000f * getFloat("beep_interval", 0.5f));
		sink_start = -1f * getFloat("sink_start", 1.5f);
		lift_start = getFloat("lift_start", lift_start);
		int appVolume = getInt("app_volume", 100);
		setAppSoundVolume(appVolume);

		lift_hz = getInt("lift_hz", lift_hz);
		sink_hz = -1 * getInt("sink_hz", sink_hz);

		prenotify_interval = Math.round(1000f * getFloat("prenotify_interval", 0.3f));
		location_history = getInt("location_history", location_history);
		heading_interval = Math.round(1000f * getFloat("heading_interval", 2f));
		tone_variation = getInt("tone_variation", tone_variation);
		baro_sensitivity = getInt("baro_sensitivity", 25);
		compass_filter_sensitivity = getFloat("compass_filter_sensitivity", compass_filter_sensitivity);
		units_system = getInt("units_system", units_system);
		max_last_thermal_distance = getInt("max_last_thermal_distance", max_last_thermal_distance);
		min_thermal_interval = 1000 * getFloat("min_thermal_interval", min_thermal_interval / 1000f);
		min_thermal_gain = getFloat("min_thermal_gain", min_thermal_gain);
	}

	private static float getFloat(String name, float defaultValue) {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			return Float.valueOf(prefs.getString(name, String.valueOf(defaultValue)));
		} catch (Exception ex) {
			Logger.get().log("Fail getting " + name);
		}
		return defaultValue;
	}

	private static int getInt(String name, int defaultValue) {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			return Integer.valueOf(prefs.getString(name, String.valueOf(defaultValue)));
		} catch (Exception ex) {
			Logger.get().log("Fail getting " + name);
		}
		return defaultValue;
	}

	private static String getString(String name, String defaultValue) {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			return prefs.getString(name, defaultValue);
		} catch (Exception ex) {
			Logger.get().log("Fail getting " + name);
		}
		return defaultValue;
	}

	private static boolean getBool(String name, boolean defaultValue) {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			return prefs.getBoolean(name, defaultValue);
		} catch (Exception ex) {
			Logger.get().log("Fail getting " + name);
		}
		return defaultValue;
	}

	public static void updateVersion(PreferencesMenu preferencesMenu) {
		try {
			Preference appVersionPreference = preferencesMenu.findPreference("appVersionName");
			PackageInfo pInfo = AVarioActivity.CONTEXT.getPackageManager().getPackageInfo(
					AVarioActivity.CONTEXT.getPackageName(), 0);
			String version = pInfo.versionName + "." + pInfo.versionCode;
			appVersionPreference.setSummary(version);
		} catch (NameNotFoundException e) {
			Logger.get().log("Fail getting version ", e);
		}
	}
}
