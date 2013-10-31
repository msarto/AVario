package org.avario.ui;

import org.avario.R;
import org.avario.engine.DataAccessObject;
import org.avario.engine.LocationsHistory;
import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.CompasConsumer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.prefs.Preferences;
import org.avario.ui.poi.PoiView;
import org.avario.ui.view.NavigationView;
import org.avario.utils.StringFormatter;

import android.app.Activity;
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

		headingMarkPaint.setAntiAlias(true);
		headingMarkPaint.setTextSize(24);
		headingMarkPaint.setFakeBoldText(true);
		headingMarkPaint.setTextAlign(Paint.Align.CENTER);
		headingMarkPaint.setColor(Color.BLUE);

	}

	private Activity context;
	private NavigationView navView;

	protected int[] canvasMargins = new int[] { 0, 0, 0, 0 };
	protected int radius = 80;
	protected int xCenter;
	protected int yCenter;

	protected Bitmap windsock;
	protected Bitmap thermal;
	protected Bitmap heading;

	private final Typeface font;
	private PoiView poiView;
	private float prevBearing = 0;

	protected NavigatorUpdater(Activity context) {
		this.context = context;
		windsock = BitmapFactory.decodeResource(context.getResources(), R.drawable.windsock);
		thermal = BitmapFactory.decodeResource(context.getResources(), R.drawable.spiral);
		heading = BitmapFactory.decodeResource(context.getResources(), R.drawable.heading);
		font = StringFormatter.getLargeFont(context.getApplicationContext());
		densityMultiplier = context.getResources().getDisplayMetrics().density;
	}

	public static void init(Activity context) {
		THIS = new NavigatorUpdater(context);
		SensorProducer.get().registerConsumer(THIS);
	}

	public static NavigatorUpdater get() {
		return THIS;
	}

	public void draw(Canvas navCanvas, int xCenter, int yCenter) {

		this.xCenter = xCenter;
		this.yCenter = yCenter;
		if (navView == null) {
			navView = (NavigationView) context.findViewById(R.id.navLayout);
			radius = (int) Math.round(navView.getWidth() / 3);
		}
		navCanvas.rotate(DataAccessObject.get().getBearing(), this.xCenter, this.yCenter);
		initDrawings(navCanvas);

		drawPOIInfo(navCanvas);
		drawWindSpeed(navCanvas);
		// Disable drawing heading this version
		// drawHeading(navCanvas);
		if (DataAccessObject.get().isGPSFix()) {
			drawThermal(navCanvas);
		} else {
			LocationsHistory.get().clearLocations();
		}

	}

	private void initDrawings(Canvas navigationCanvas) {
		Paint circlePaint = new Paint();
		circlePaint.setColor(DataAccessObject.get().isGPSFix() ? Color.BLACK : Color.RED);
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setStrokeWidth(3 * densityMultiplier);
		circlePaint.setAntiAlias(true);
		navigationCanvas.drawCircle(xCenter, yCenter, radius, circlePaint);

		Paint cardinals = new Paint();
		cardinals.setAntiAlias(true);

		cardinals.setTextSize(30 * densityMultiplier);
		cardinals.setColor(Color.BLACK);
		cardinals.setTextAlign(Paint.Align.CENTER);
		cardinals.setTypeface(font);

		navigationCanvas.drawText(context.getString(R.string.north), xCenter,
				yCenter - Math.round(radius + 5 * densityMultiplier), cardinals);
		navigationCanvas.drawText(context.getString(R.string.south), xCenter,
				yCenter + Math.round(radius + 25 * densityMultiplier), cardinals);
		navigationCanvas.drawText(context.getString(R.string.west),
				xCenter - Math.round(radius + 15 * densityMultiplier), yCenter, cardinals);
		navigationCanvas.drawText(context.getString(R.string.east),
				xCenter + Math.round(radius + 15 * densityMultiplier), yCenter, cardinals);
		poiView = new PoiView(context, navigationCanvas, xCenter, yCenter);
	}

	private void drawWindSpeed(Canvas navCanvas) {
		final float bearing = DataAccessObject.get().getWindDirectionBearing();
		if (bearing != 0.0f) {
			float angle = (float) (bearing * Math.PI / 180f);
			// angle = (float) (Math.PI - angle);
			float theX = (float) ((radius + 15) * Math.cos(angle) * densityMultiplier + xCenter);
			float theY = (float) ((radius + 15) * Math.sin(angle) * densityMultiplier + yCenter);
			navCanvas.save();
			// navCanvas.rotate(360 - DataAccessObject.get().getBearing(),
			// xCenter, yCenter);
			navCanvas.drawBitmap(windsock, theX, theY, windMarkPaint);
			navCanvas.restore();
		}
	}

	private void drawHeading(Canvas navCanvas) {
		final float heading = DataAccessObject.get().getHeading();
		if (heading != 0.0f) {
			float angle = (float) (heading * Math.PI / 180f);
			angle = (float) (Math.PI - angle);
			float theX = (float) (95 * Math.cos(angle) + xCenter);
			float theY = (float) (95 * Math.sin(angle) + yCenter);
			navCanvas.save();
			navCanvas.drawBitmap(this.heading, theX, theY, headingMarkPaint);
			navCanvas.restore();
		}
	}

	private void drawThermal(Canvas navCanvas) {
		final Location lastLocation = DataAccessObject.get().getLastlocation();
		final Location lastThermal = DataAccessObject.get().getLastThermal();
		if (lastThermal != null && lastLocation != null) {
			float thermalBearing = lastLocation.bearingTo(lastThermal);
			float angle = (float) ((thermalBearing % 360) * Math.PI / 180f);
			float distanceToThermal = lastLocation.distanceTo(lastThermal);
			if (distanceToThermal < Preferences.max_last_thermal_distance) {
				distanceToThermal = distanceToThermal > 100 ? 100 : distanceToThermal;
				angle = (float) (Math.PI - angle);
				float theX = (float) (distanceToThermal * Math.cos(angle) + xCenter);
				float theY = (float) (distanceToThermal * Math.sin(angle) + yCenter);
				navCanvas.save();
				// navCanvas.rotate(360 - DataAccessObject.get().getBearing(),
				// xCenter, yCenter);
				navCanvas.drawBitmap(thermal, theX, theY, lastThermalPaint);
				navCanvas.restore();
				// navCanvas.drawCircle(theY, theX, 8, lastThermalPaint);
			}
		}
	}

	private void drawPOIInfo(Canvas navCanvas) {
		poiView.drawActivePoi();
	}

	@Override
	public void notifyWithLocation(Location location) {
		if (navView != null) {
			navView.invalidate();
		}
	}

	@Override
	public void notifyNorth(float bearing) {
		if (navView != null
				&& Math.abs(prevBearing - DataAccessObject.get().getBearing()) >= Preferences.compass_filter_sensitivity) {
			prevBearing = DataAccessObject.get().getBearing();
			navView.invalidate();
		}
	}
}
