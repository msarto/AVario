package org.avario.ui;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.CompasConsumer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.ui.poi.PoiView;
import org.avario.ui.view.NavigationView;
import org.avario.utils.StringFormatter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;

public class NavigatorUpdater implements LocationConsumer, CompasConsumer {
	private static NavigatorUpdater THIS;
	private static Paint lastThermalPaint = new Paint();
	private static Paint windMarkPaint = new Paint();
	private static Paint headingMarkPaint = new Paint();
	private static Paint gForcePaint = new Paint();
	private Paint cardinals = new Paint();
	private Paint circlePaint = new Paint();
	private float densityMultiplier = 1;

	static {
		lastThermalPaint.setAntiAlias(true);
		lastThermalPaint.setStrokeWidth(6);
		lastThermalPaint.setTextSize(16);
		lastThermalPaint.setTextAlign(Paint.Align.RIGHT);
		lastThermalPaint.setColor(Color.GREEN);

		windMarkPaint.setAntiAlias(true);
		windMarkPaint.setTextSize(35);
		windMarkPaint.setFakeBoldText(true);
		windMarkPaint.setTextAlign(Paint.Align.CENTER);
		windMarkPaint.setColor(Color.YELLOW);

		gForcePaint.setAntiAlias(true);
		gForcePaint.setTextSize(18);
		gForcePaint.setFakeBoldText(true);
		// gForcePaint.setTextAlign(Paint.Align.CENTER);
		gForcePaint.setColor(Color.BLACK);

		headingMarkPaint.setAntiAlias(true);
		headingMarkPaint.setTextSize(24);
		headingMarkPaint.setFakeBoldText(true);
		headingMarkPaint.setTextAlign(Paint.Align.CENTER);
		headingMarkPaint.setColor(Color.BLUE);
	}

	private NavigationView navView;

	protected int radius = 80;
	protected int xCenter;
	protected int yCenter;

	protected Bitmap windsock;
	protected Bitmap thermal;
	protected Bitmap heading;

	private final Typeface font;
	private PoiView poiView;
	private float prevBearing = 0;

	protected NavigatorUpdater() {
		windsock = BitmapFactory.decodeResource(AVarioActivity.CONTEXT.getResources(), R.drawable.windsock);
		thermal = BitmapFactory.decodeResource(AVarioActivity.CONTEXT.getResources(), R.drawable.spiral);
		heading = BitmapFactory.decodeResource(AVarioActivity.CONTEXT.getResources(), R.drawable.heading);
		font = StringFormatter.getLargeFont(AVarioActivity.CONTEXT.getApplicationContext());
		densityMultiplier = AVarioActivity.CONTEXT.getResources().getDisplayMetrics().density;

		cardinals.setAntiAlias(true);
		cardinals.setTextSize(30 * densityMultiplier);
		cardinals.setColor(Color.BLACK);
		cardinals.setTextAlign(Paint.Align.CENTER);
		cardinals.setTypeface(font);

		circlePaint.setColor(Color.RED);
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setStrokeWidth(3 * densityMultiplier);
		circlePaint.setAntiAlias(true);
	}

	public static void init() {
		clear();
		THIS = new NavigatorUpdater();
		SensorProducer.get().registerConsumer(THIS);
	}

	public static void clear() {
		if (THIS != null) {
			SensorProducer.get().unregisterConsumer(THIS);
		}
	}

	public static NavigatorUpdater get() {
		return THIS;
	}

	public void draw(Canvas navCanvas, int xCenter, int yCenter) {
		this.xCenter = xCenter;
		this.yCenter = yCenter;
		// drawGForce(navCanvas);
		navCanvas.rotate(DataAccessObject.get().getBearing(), this.xCenter, this.yCenter);
		if (navView == null) {
			navView = (NavigationView) AVarioActivity.CONTEXT.findViewById(R.id.navLayout);
			radius = (int) Math.round(navView.getWidth() / 3);
			poiView = new PoiView(AVarioActivity.CONTEXT, navCanvas, xCenter, yCenter);
		}

		drawCompass(navCanvas);
		drawPOIInfo(navCanvas);

		drawIcon(navCanvas, DataAccessObject.get().getWindDirectionBearing(), windsock);
		// Disable drawing heading this version
		drawIcon(navCanvas, DataAccessObject.get().getHeading(), heading);
		if (DataAccessObject.get().isGPSFix()) {
			drawThermal(navCanvas);
		}
	}

	private void drawCompass(Canvas navigationCanvas) {
		circlePaint.setColor(DataAccessObject.get().isGPSFix() ? Color.BLACK : Color.RED);
		navigationCanvas.drawCircle(xCenter, yCenter, radius, circlePaint);

		navigationCanvas.drawText(AVarioActivity.CONTEXT.getString(R.string.north), xCenter,
				yCenter - Math.round(radius + 5 * densityMultiplier), cardinals);
		navigationCanvas.drawText(AVarioActivity.CONTEXT.getString(R.string.south), xCenter,
				yCenter + Math.round(radius + 25 * densityMultiplier), cardinals);
		navigationCanvas.drawText(AVarioActivity.CONTEXT.getString(R.string.west),
				xCenter - Math.round(radius + 15 * densityMultiplier), yCenter, cardinals);
		navigationCanvas.drawText(AVarioActivity.CONTEXT.getString(R.string.east),
				xCenter + Math.round(radius + 15 * densityMultiplier), yCenter, cardinals);
	}

	private void drawIcon(Canvas navCanvas, double bearing, Bitmap bitmap) {
		if (bearing != -1) {
			bearing = bearing % 360;
			float angle = (float) (bearing * Math.PI / 180f - Math.PI / 2);
			float theX = (float) ((radius + 5 * densityMultiplier) * Math.cos(angle) + xCenter);
			float theY = (float) ((radius + 5 * densityMultiplier) * Math.sin(angle) + yCenter);
			navCanvas.save();
			navCanvas.drawBitmap(bitmap, theX, theY, windMarkPaint);
			navCanvas.restore();
		}
	}

	private void drawThermal(Canvas navCanvas) {
		final Location lastLocation = DataAccessObject.get().getLastlocation();
		final Location lastThermal = DataAccessObject.get().getLastThermal();
		if (lastThermal != null && lastLocation != null) {
			float thermalBearing = lastLocation.bearingTo(lastThermal);
			float angle = (float) ((thermalBearing % 360) * Math.PI / 180f - Math.PI / 2);
			float distanceToThermal = lastLocation.distanceTo(lastThermal);
			if (distanceToThermal < Preferences.max_last_thermal_distance) {
				float maxThermalUIDistance = radius + 15 * densityMultiplier;
				distanceToThermal = distanceToThermal > maxThermalUIDistance ? maxThermalUIDistance : distanceToThermal;
				angle = (float) (Math.PI - angle);
				float theX = (float) (distanceToThermal * Math.cos(angle) + xCenter);
				float theY = (float) (distanceToThermal * Math.sin(angle) + yCenter);
				navCanvas.save();
				navCanvas.drawBitmap(thermal, theX, theY, lastThermalPaint);
				navCanvas.restore();
			}
		}
	}

	private void drawPOIInfo(Canvas navCanvas) {
		poiView.drawActivePoi();
	}

	private void drawGForce(Canvas navCanvas) {
		navCanvas.save();
		navCanvas.translate(xCenter, yCenter);
		double gForce = DataAccessObject.get().getGForce();
		navCanvas.drawText(StringFormatter.noDecimals((float) gForce) + " G",
				-xCenter + Math.round(10 * densityMultiplier), yCenter - Math.round(10 * densityMultiplier),
				gForcePaint);
		navCanvas.restore();
	}

	@Override
	public void notifyWithLocation(Location location) {

		AVarioActivity.CONTEXT.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (navView != null) {
					navView.invalidate();
				}
			}
		});
	}

	@Override
	public void notifyNorth(float bearing) {
		if (navView != null
				&& Math.abs(prevBearing - DataAccessObject.get().getBearing()) >= Preferences.compass_filter_sensitivity) {
			prevBearing = DataAccessObject.get().getBearing();
			AVarioActivity.CONTEXT.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					navView.invalidate();
				}
			});
		}
	}
}
