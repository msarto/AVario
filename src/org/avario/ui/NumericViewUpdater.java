package org.avario.ui;

import java.util.Calendar;

import org.avario.R;
import org.avario.engine.DataAccessObject;
import org.avario.engine.LocationsHistory;
import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.BarometerConsumer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.StringFormatter;
import org.avario.utils.UnitsConverter;

import android.app.Activity;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.TextView;

public class NumericViewUpdater extends AsyncTask<Integer, Integer, Integer> implements LocationConsumer, BarometerConsumer {
	private Activity context = null;
	private static NumericViewUpdater THIS;
	protected long startTime = System.currentTimeMillis();
	protected TextView altitudeView = null;
	protected TextView altitudeMeasure = null;
	protected TextView groundSpeedView = null;
	protected TextView groundSpeedMeasure = null;
	protected TextView lastGainView = null;
	protected TextView lastGainText = null;
	protected TextView timeSpanView = null;
	protected TextView recView = null;
	protected TextView clockView = null;

	protected Location lastLocationTracked;
	protected float metersTracked = 0;
	protected TextView flightDistance = null;

	protected NumericViewUpdater(Activity context) {
		this.context = context;
		final Typeface font = StringFormatter.getLargeFont(context.getApplicationContext());
		altitudeView = (TextView) context.findViewById(R.id.altValue);
		altitudeView.setTypeface(font);
		altitudeMeasure = (TextView) context.findViewById(R.id.altMeasure);
		groundSpeedView = (TextView) context.findViewById(R.id.gspeedValue);
		groundSpeedView.setTypeface(font);
		groundSpeedMeasure = (TextView) context.findViewById(R.id.gspeedMeasure);
		groundSpeedMeasure.setText(UnitsConverter.preferredDistLong() + "/h");
		recView = (TextView) context.findViewById(R.id.rec_status);
		recView.setText(Preferences.units_system == 1 ? R.string.ms : R.string.fs);
		lastGainView = (TextView) context.findViewById(R.id.glastValue);
		lastGainView.setTypeface(font);
		lastGainText = (TextView) context.findViewById(R.id.glastText);
		timeSpanView = (TextView) context.findViewById(R.id.ftimeValue);
		clockView = (TextView) context.findViewById(R.id.clock);
		flightDistance = (TextView) context.findViewById(R.id.kmValue);
		lastGainText.setText(String.format(context.getString(R.string.gainedlastmin), String.valueOf(Preferences.location_history)));
	}

	public static void init(Activity context) {
		THIS = new NumericViewUpdater(context);
		THIS.execute();
		SensorProducer.get().registerConsumer(THIS);
	}

	@Override
	public void notifyWithLocation(Location location) {
		if (location.hasSpeed()) {
			groundSpeedView
					.setText(StringFormatter.noDecimals(UnitsConverter.toPreferredLong(UnitsConverter.msTokmh(location.getSpeed()))));
			groundSpeedMeasure.setText(UnitsConverter.preferredDistLong() + "/h");
		}
		lastGainText.setText(String.format(context.getString(R.string.gainedlastmin), String.valueOf(Preferences.location_history)));
		double lastGain = UnitsConverter.toPreferredShort(Math.round(LocationsHistory.get().getHistoryAltimeterGain()));
		lastGainView.setText(context.getApplicationContext().getString(R.string.lastgainvalue, StringFormatter.noDecimals(lastGain)));
		if (lastLocationTracked != null) {
			metersTracked += location.distanceTo(lastLocationTracked);
			flightDistance.setText(StringFormatter.oneDecimal(UnitsConverter.toPreferredLong(metersTracked / 1000F))
					+ UnitsConverter.preferredDistLong());
		}
		lastLocationTracked = location;
	}

	@Override
	public void notifyWithAltFromPreasure(float altitude) {
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		while (true) {
			try {
				publishProgress();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				Logger.get().log("Fail to refresh UI", e);
				break;
			}
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(final Integer... progress) {
		try {
			timeSpanView.setText(UnitsConverter.timeSpan(startTime));
			clockView.setText(String.format("%1$tH:%1$tM:%1$tS", Calendar.getInstance()));
			groundSpeedMeasure.setText(UnitsConverter.preferredDistLong() + "/h");
			altitudeView.setText(StringFormatter.noDecimals(UnitsConverter.toPreferredShort(DataAccessObject.get().getLastAltitude())));
			altitudeMeasure.setText(context.getApplicationContext().getString(
					Preferences.units_system == 2 ? R.string.feet : R.string.meters));
		} catch (Exception e) {
			Logger.get().log("Fail to refresh UI progress", e);
		}
	}

	public static void clear() {
		THIS.cancel(true);
	}
}
