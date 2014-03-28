package org.avario.engine.tracks;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.BarometerConsumer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.TonePlayer;
import org.avario.engine.sounds.TonePlayer.ToneType;
import org.avario.utils.Logger;

import android.app.Activity;
import android.location.Location;
import android.os.Environment;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class Tracker implements LocationConsumer, BarometerConsumer {
	private static Tracker THIS = new Tracker();
	private FileOutputStream trackStream = null;
	private Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	private Location lastNotification;
	private TextView recView;
	private boolean tracking = false;
	private boolean needTracking = false;
	private TrackInfo metaInfo = new TrackInfo();
	private String trackFileName;
	private static final Animation recAnimation = new AlphaAnimation(0.0f, 1.0f);

	protected Tracker() {
	}

	public static Tracker get() {
		return THIS;
	}

	public static void init(Activity context) {
		THIS = new Tracker();
		THIS.recView = (TextView) context.findViewById(R.id.rec_status);
		SensorProducer.get().registerConsumer(THIS);
	}

	public boolean startTracking() {
		synchronized (recAnimation) {
			float speed = DataAccessObject.get().getLastlocation().getSpeed();
			if (tracking == false && speed > 3) {
				Logger.get().log("Start tracking " + tracking);
				TonePlayer startTrack = new TonePlayer();
				for (int i = 0; i < 3; i++) {
					startTrack.play(400f, ToneType.HIGH);
					startTrack.stop();
				}
				trackStream = null;
				cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
				lastNotification = null;
				metaInfo = new TrackInfo();
				trackFileName = null;
				startTrack();
				tracking = true;
			}

			recView.setText(R.string.rec);
			recAnimation.setDuration(500);
			recAnimation.setStartOffset(20);
			recAnimation.setRepeatMode(Animation.REVERSE);
			recAnimation.setRepeatCount(Animation.INFINITE);
			recView.startAnimation(recAnimation);

			needTracking = !tracking;
			return tracking;
		}
	}

	public synchronized void stopTracking() {
		Logger.get().log("Stop tracking " + tracking);
		if (tracking == true) {
			tracking = false;
			stopTrack();
			recView.setText(Preferences.units_system == 1 ? R.string.ms : R.string.fs);
			recAnimation.setRepeatCount(0);
		}
	}

	public boolean isTracking() {
		return tracking;
	}

	protected void startTrack() {
		try {
			String HEADER = "AXMP Sony Xperia Active - AVario 0.1\r\n";
			HEADER += "HFDTE" + String.format("%1$td%1$tm%1$ty", new GregorianCalendar(TimeZone.getTimeZone("GMT")))
					+ "\r\n";
			HEADER += "HFFXA50\r\n";
			HEADER += "HOPLTPILOT: AVario\r\n";
			HEADER += "HFFTYFR TYPE:AVario Android\r\n";
			HEADER += "HFGPS: Internal GPS (Android)\r\n";
			HEADER += "HODTM100GPSDATUM: WGS-84\r\n";
			HEADER += "HOCCLCOMPETITION CLASS: Paraglider open\r\n";
			HEADER += "I013638GSP\r\n";
			trackFileName = String.format("%1$ty%1$tm%1$td%1$tH%1$tM%1$tS", new GregorianCalendar());
			final File trackFile = new File(Environment.getExternalStorageDirectory() + File.separator + "AVario"
					+ File.separator + trackFileName + ".igc");
			if (!trackFile.getParentFile().exists()) {
				trackFile.getParentFile().mkdirs();
			}

			Logger.get().log("Start writting " + trackFile.getAbsolutePath());
			trackStream = new FileOutputStream(trackFile);
			trackStream.write(HEADER.getBytes());
			metaInfo.setFlightStart(System.currentTimeMillis());

		} catch (Exception e) {
			Logger.get().log("Fail starting track ", e);
		}
	}

	protected void stopTrack() {
		if (trackStream != null) {
			try {
				if (lastNotification != null) {
					metaInfo.setEndAlt((int) Math.round(lastNotification.getAltitude()));
				}
				if (trackFileName != null) {
					File trackMetaFile = new File(Environment.getExternalStorageDirectory() + File.separator + "AVario"
							+ File.separator + trackFileName + ".meta");
					metaInfo.writeTo(trackMetaFile);
				}

				trackStream.close();
				Logger.get().log("Track closed");
			} catch (Exception e) {
				Logger.get().log("Fail closed track ", e);
			}
		}
	}

	@Override
	public synchronized void notifyWithLocation(final Location location) {
		// Start the track if selected
		if (needTracking || (Preferences.auto_track && !isTracking())) {
			AVarioActivity.CONTEXT.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					startTracking();
				}
			});
			return;
		}

		if (trackStream != null && tracking) {
			String strSeq = null;
			try {
				cal.setTimeInMillis(location.getTime());
				if (lastNotification != null && (location.getTime() - lastNotification.getTime() < 1000)) {
					return;
				}
				int altitude = (int) (location.hasAltitude() ? location.getAltitude() : DataAccessObject.get()
						.getLastAltitude());

				strSeq = String.format(Locale.US, "B%02d%02d%02d%s%s%c%05d%05d%03d\r\n", cal.get(Calendar.HOUR_OF_DAY),
						cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), degreeStr(location.getLatitude(), true),
						degreeStr(location.getLongitude(), false), 'A', altitude, altitude, (int) location.getSpeed());

				trackStream.write(strSeq.getBytes());
				updateMetaInfo(location);
				lastNotification = location;
			} catch (Exception e) {
				Logger.get().log("Fail writing seq: " + strSeq, e);
			}
		}
	}

	private void updateMetaInfo(Location location) {
		if (metaInfo.getStartAlt() == 0) {
			metaInfo.setStartAlt((int) Math.round(location.getAltitude()));
		}
		if (metaInfo.getHighestAlt() < location.getAltitude()) {
			metaInfo.setHighestAlt((int) Math.round(location.getAltitude()));
		}
		if (location.hasSpeed() && metaInfo.getMaxSpeed() < location.getSpeed()) {
			metaInfo.setMaxSpeed(location.getSpeed());
		}
		if (metaInfo.getHighestVSpeed() < DataAccessObject.get().getLastVSpeed()) {
			metaInfo.setHighestVSpeed(DataAccessObject.get().getLastVSpeed());
		}
		if (metaInfo.getHighestSink() > DataAccessObject.get().getLastVSpeed()) {
			metaInfo.setHighestSink(DataAccessObject.get().getLastVSpeed());
		}
		if (lastNotification != null) {
			metaInfo.setFlightDuration(metaInfo.getFlightDuration() + (location.getTime() - lastNotification.getTime()));
			metaInfo.setFlightLenght(Math.round(metaInfo.getFlightLenght() + location.distanceTo(lastNotification)));
		}

	}

	private String degreeStr(double degIn, boolean isLatitude) {
		boolean isPos = degIn >= 0;
		char dirLetter = isLatitude ? (isPos ? 'N' : 'S') : (isPos ? 'E' : 'W');

		degIn = Math.abs(degIn);
		double minutes = 60 * (degIn - Math.floor(degIn));
		degIn = Math.floor(degIn);
		int minwhole = (int) minutes;
		int minfract = (int) ((minutes - minwhole) * 1000);

		return String.format(Locale.US, (isLatitude ? "%02d" : "%03d") + "%02d%03d%c", (int) degIn, minwhole, minfract,
				dirLetter);
	}

	@Override
	public synchronized void notifyWithAltFromPreasure(float altitude) {
		if (tracking) {
			if (metaInfo.getHighestAlt() < altitude) {
				metaInfo.setHighestAlt((int) Math.round(altitude));
			}
			if (metaInfo.getHighestVSpeed() < DataAccessObject.get().getLastVSpeed()) {
				metaInfo.setHighestVSpeed(DataAccessObject.get().getLastVSpeed());
			} else if (metaInfo.getHighestSink() > DataAccessObject.get().getLastVSpeed()) {
				metaInfo.setHighestSink(DataAccessObject.get().getLastVSpeed());
			}
		}
	}

}
