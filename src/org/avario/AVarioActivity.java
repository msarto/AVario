package org.avario;

import org.avario.engine.SensorProducer;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.poi.PoiManager;
import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.BeepBeeper;
import org.avario.engine.tracks.Tracker;
import org.avario.inappbilling.Donate;
import org.avario.ui.NavigatorUpdater;
import org.avario.ui.NumericViewUpdater;
import org.avario.ui.VarioMeterScaleUpdater;
import org.avario.ui.poi.PoiList;
import org.avario.ui.prefs.PreferencesMenu;
import org.avario.ui.tracks.TracksList;
import org.avario.utils.Logger;
import org.avario.utils.Speaker;
import org.avario.utils.bt.BTScanner;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * 
 */
public class AVarioActivity extends Activity {
	public static Activity CONTEXT;
	private PowerManager.WakeLock wakeLock;
	private int startVolume = Integer.MIN_VALUE;
	private boolean viewCreated = false;

	public AVarioActivity() {
		super();
		AVarioActivity.CONTEXT = this;
	}

	protected void initializeSensors() {
		super.onStart();
		try {

			DataAccessObject.init();
			setContentView(R.layout.vario);
			if (Preferences.use_sensbox) {
				boolean canUseBT = BTScanner.get().scan();
				// Try using internal sensors if the BT are not available;
				SensorProducer.init(this, !canUseBT);
			} else {
				SensorProducer.init(this, true);
			}
			Tracker.init(this);
			PoiManager.init();
			Speaker.init();
			// Draw the UI from the vario.xml layout

			NavigatorUpdater.init();
			NumericViewUpdater.init();
			VarioMeterScaleUpdater.init();
			BeepBeeper.init();

			addNotification();
		} catch (Throwable ex) {
			Logger.get().log("Fail initializing ", ex);
		}
	}

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		try {
			if (viewCreated) {
				return;
			}
			Preferences.update(this);
			if (Preferences.orientation == 2) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			// Keep the screen awake
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "AVario lock");
			wakeLock.acquire();

			viewCreated = true;
			AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			startVolume = audio.getStreamVolume(Preferences.STREAM_TYPE);

			Logger.init();
			initializeSensors();
			Donate.get().init();
		} catch (Throwable e) {
			Logger.get().log("Fail on create ", e);
		}
	}

	private void addNotification() {
		try {
			Notification notification = new Notification(R.drawable.icon, "AVario", System.currentTimeMillis());
			notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			NotificationManager notifier = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			Intent intent = new Intent(this, AVarioActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

			// Set the info for the views that show in the notification panel.
			notification.setLatestEventInfo(this, "AVario", "AVario", contentIntent);

			notifier.notify(22313, notification);
		} catch (Throwable ex) {
			Logger.get().log("Fail placing notification icon " + ex.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean bRet = super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		MenuItem trackingMenu = menu.findItem(R.id.ontrack);
		if (trackingMenu != null) {
			trackingMenu.setTitle(Tracker.get().isTracking() ? R.string.stoptracking : R.string.starttracking);
		}
		return bRet;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preferences:
			startActivityForResult(new Intent(this, PreferencesMenu.class), 1);
			return true;
		case R.id.tracks:
			startActivityForResult(new Intent(this, TracksList.class), 2);
			return true;
		case R.id.poi:
			startActivityForResult(new Intent(this, PoiList.class), 3);
			return true;
		case R.id.ontrack:
			if (Tracker.get().isTracking()) {
				Tracker.get().stopTracking();
				item.setTitle(R.string.starttracking);
			} else {
				Tracker.get().startTracking();
				item.setTitle(R.string.stoptracking);
			}
			return true;
		case R.id.exit:
			// A bit brutal ....
			System.runFinalizersOnExit(true);
			onDestroy();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		// To disable back button
		Toast.makeText(this, R.string.exit_from_menu, Toast.LENGTH_SHORT).show();
		openOptionsMenu();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			Logger.get().log("Home Button Clicked");
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void clear() {
		try {
			if (wakeLock != null) {
				wakeLock.release();
			}
			Tracker.get().stopTracking();
			undoSoundVolume();
			Speaker.clear();
			SensorProducer.clear();
			BeepBeeper.clear();
			NumericViewUpdater.clear();
			DataAccessObject.clear();
			if (Preferences.use_sensbox) {
				BTScanner.get().clear();
			}
			Logger.get().log("App terminated...");
			Logger.get().close();
		} catch (Throwable ex) {
			Logger.get().log("Fail terminating awake " + ex.getMessage());
		}
	}

	@Override
	public void onDestroy() {
		clear();
		super.onDestroy();
	}

	private void undoSoundVolume() {
		try {
			NotificationManager notifier = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notifier.cancel(22313);
			if (startVolume != Integer.MIN_VALUE) {
				AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				audio.setStreamVolume(Preferences.STREAM_TYPE, startVolume, 0);
			}
		} catch (Throwable ex) {
			Logger.get().log("Fail to restore volume", ex);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BTScanner.INTENT_ID) {
			BTScanner.get().onActivityResult(requestCode, resultCode, data);
		}
	}

}
