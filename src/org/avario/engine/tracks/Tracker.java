package org.avario.engine.tracks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.Signature;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.avario.AVarioActivity;
import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.tone.TonePlayer;
import org.avario.engine.sounds.tone.TonePlayer.ToneType;
import org.avario.ui.NumericViewUpdater;
import org.avario.utils.IOUtils;
import org.avario.utils.Logger;

import android.app.Activity;
import android.location.Location;
import android.os.Build;
import android.util.Base64;

public class Tracker implements LocationConsumer {
	private static Tracker THIS;
	private OutputStream trackStream = null;
	private Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	private Location lastNotification;
	private boolean tracking = false;
	private boolean needTracking = false;
	private TrackInfo metaInfo = new TrackInfo();
	private String trackFileName;
	private Signature sign;
	private boolean useSignature = false;

	protected Tracker() {
	}

	public static Tracker get() {
		return THIS;
	}

	public static void init(Activity context) {
		if (THIS == null) {
			THIS = new Tracker();
			SensorProducer.get().registerConsumer(THIS);
		}
	}

	public synchronized boolean startTracking() {
		if (tracking == false && DataAccessObject.get().isInFlight()) {
			Logger.get().log("Start tracking " + tracking);
			initSignature();
			trackStream = null;
			cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			lastNotification = null;
			metaInfo = new TrackInfo();
			trackFileName = null;
			tracking = startTrack();
		}
		if (tracking) {
			NumericViewUpdater.getInstance().notifyStartTracking();
		}
		needTracking = !tracking;
		return tracking;
	}

	private boolean initSignature() {
		/*
		 * try {
		 * 
		 * sign = Signature.getInstance("SHA1withRSA"); KeyFactory fac =
		 * KeyFactory.getInstance("RSA"); EncodedKeySpec privKeySpec = new
		 * PKCS8EncodedKeySpec(Base64.decode(igcK, Base64.DEFAULT)); PrivateKey
		 * pk = fac.generatePrivate(privKeySpec); sign.initSign(pk); return
		 * true; } catch (Throwable e) {
		 * Logger.get().log("Can not make a signature", e); }
		 */
		return false;
	}

	public synchronized void stopTracking() {
		Logger.get().log("Stop tracking " + tracking);
		needTracking = false;
		if (tracking == true) {
			tracking = false;
			stopTrack();
		}
		NumericViewUpdater.getInstance().notifyStopTracking();
	}

	public boolean isTracking() {
		return tracking || needTracking;
	}

	private boolean startTrack() {
		boolean bRet = false;
		try {
			String HEADER = "AXMP " + Build.MANUFACTURER + " " + Build.MODEL + "\r\n";
			HEADER += "HFDTE" + String.format("%1$td%1$tm%1$ty", new GregorianCalendar(TimeZone.getTimeZone("GMT")))
					+ "\r\n";
			HEADER += "HFFXA50\r\n";
			HEADER += "HOPLTPILOT: AVario\r\n";
			HEADER += "HFFTYFR TYPE:AVario Android\r\n";
			HEADER += "HFGPS: Internal GPS (Android)\r\n";
			HEADER += "HODTM100GPSDATUM: WGS-84\r\n";
			HEADER += "HOCCLCOMPETITION CLASS: Paraglider open\r\n";
			HEADER += "HFRFWFIRMWAREVERSION: " + Preferences.getAppVersion() + "\r\n";
			HEADER += "I013638GSP\r\n";
			trackFileName = String.format("%1$ty%1$tm%1$td%1$tH%1$tM%1$tS", new GregorianCalendar());
			final File trackFile = new File(IOUtils.getStorageDirectory(), trackFileName + ".igc");
			IOUtils.createParentIfNotExists(trackFile);

			Logger.get().log("Start writting " + trackFile.getAbsolutePath());
			trackStream = initSignature() ? new SignedOutputStream(new FileOutputStream(trackFile), sign)
					: new BufferedOutputStream(new FileOutputStream(trackFile));
			trackStream.write(HEADER.getBytes());
			metaInfo.setFlightStart(System.currentTimeMillis());
			bRet = true;
		} catch (Throwable e) {
			Logger.get().log("Fail starting track ", e);
		}
		return bRet;
	}

	private void stopTrack() {
		if (trackStream != null) {
			try {

				if (lastNotification != null) {
					metaInfo.setEndAlt((int) Math.round(lastNotification.getAltitude()));
				}
				if (trackFileName != null) {
					File trackMetaFile = new File(AVarioActivity.CONTEXT.getFilesDir(), trackFileName + ".meta");
					metaInfo.writeTo(trackMetaFile);
				}

				if (useSignature) {
					// Write the G record
					final String sigStr = Base64.encodeToString(sign.sign(), Base64.DEFAULT).replaceAll("[\\r\\n]", "");
					final short baseSign = 64;
					StringBuilder sb = new StringBuilder("G");
					for (int i = 0; i < sigStr.length() / baseSign; i++) {
						sb.append(sigStr.substring(i * baseSign, i * baseSign + baseSign));
					}
					if (sigStr.length() % baseSign > 0) {
						sb.append(sigStr.substring(((int) (sigStr.length() / baseSign)) * baseSign));
					}
					trackStream.write(sb.toString().getBytes());
				}
				trackStream.flush();
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
		try {
			if (needTracking || (Preferences.auto_track && !isTracking())) {
				boolean trackSound = startTracking();
				if (trackSound) {
					TonePlayer startTrack = new TonePlayer();
					for (int i = 0; i < 3; i++) {
						startTrack.play(400f, ToneType.HIGH);
						startTrack.stop();
						Thread.sleep(100);
					}
				}
				return;
			}

			if (tracking && !DataAccessObject.get().isInFlight()) {
				// Not in flight anymore
				stopTracking();
				TonePlayer startTrack = new TonePlayer();
				for (int i = 0; i < 3; i++) {
					startTrack.play(400f, ToneType.HIGH);
					startTrack.stop();
					Thread.sleep(100);
				}
				return;
			}

			Location loc = DataAccessObject.get().getLastlocation();
			if (trackStream != null && tracking) {
				String strSeq = null;
				try {
					cal.setTimeInMillis(loc.getTime());
					if (lastNotification != null && (loc.getTime() - lastNotification.getTime() < 1000)) {
						return;
					}
					int altitude = Math.round(DataAccessObject.get().getLastAltitude());
					int gpsAltitude = Math.round(DataAccessObject.get().getGPSAltitude());
					strSeq = String.format(Locale.US, "B%02d%02d%02d%s%s%c%05d%05d%03d\r\n",
							cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND),
							degreeStr(loc.getLatitude(), true), degreeStr(loc.getLongitude(), false), 'A', altitude,
							gpsAltitude, (int) loc.getSpeed());

					trackStream.write(strSeq.getBytes());
					updateMetaInfo(loc);
					updateHeight(DataAccessObject.get().getLastAltitude());
					lastNotification = loc;
				} catch (Exception e) {
					Logger.get().log("Fail writing seq: " + strSeq, e);
				}
			}
		} catch (Exception e) {
			Logger.get().log("Fail handling track", e);
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

	protected String getSignature() {
		String gSign = "G";
		return gSign;
	}

	private void updateHeight(float altitude) {
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
