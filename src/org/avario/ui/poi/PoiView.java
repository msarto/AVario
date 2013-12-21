package org.avario.ui.poi;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.poi.POI;
import org.avario.engine.poi.PoiManager;
import org.avario.utils.UnitsConverter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.Location;

public class PoiView {

	private static Paint mPaint;
	private static Paint pDistance;
	private static Paint pName;
	static {
		pDistance = new Paint();
		pDistance.setAntiAlias(true);
		pDistance.setTextSize(30);
		pDistance.setColor(Color.BLACK);
		pDistance.setTextAlign(Paint.Align.CENTER);
		pDistance.setDither(true);
		pDistance.setSubpixelText(true);

		pName = new Paint();
		pName.setAntiAlias(true);
		pName.setTextSize(15);
		pName.setColor(Color.BLACK);
		// pName.setTextAlign(Paint.Align.CENTER);
		pName.setDither(true);
		pName.setSubpixelText(true);

		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setStrokeWidth(1);
		mPaint.setDither(true);
		mPaint.setAntiAlias(true);
	}

	private Path mPath = new Path();
	private Canvas canvas;
	private int xCenter;
	private int yCenter;

	public PoiView(Context context, Canvas canvas, int xCenter, int yCenter) {
		this.canvas = canvas;
		this.xCenter = xCenter;
		this.yCenter = yCenter;

		// Construct a wedge-shaped path
		mPath.moveTo(0, -50);
		mPath.lineTo(-20, 60);
		mPath.lineTo(0, 50);
		mPath.lineTo(20, 60);
		mPath.close();

	}

	public void drawActivePoi() {
		Location lastLocation = DataAccessObject.get().getLastlocation();
		if (lastLocation != null) {
			POI activePOI = PoiManager.get().getActivePOI();
			if (activePOI != null) {
				
				canvas.save();
				canvas.translate(xCenter, yCenter);
				float bearing = activePOI.bearingTo(lastLocation);
				canvas.rotate(bearing);
				canvas.drawPath(mPath, mPaint);
				canvas.restore();

				// Now draw the distance number and keep it still
				float distance = activePOI.distanceTo(lastLocation);
				String poiName = activePOI.getName().replace("(www)", "").trim();
				poiName = poiName.length() > 10 ? poiName.substring(0, 9) + "..." : poiName;
				String distStr = UnitsConverter.normalizedDistance(distance);
				canvas.save();
				canvas.translate(xCenter, yCenter);
				canvas.rotate(360 - DataAccessObject.get().getBearing());
				canvas.drawText(distStr, 0, 0, pDistance);
				canvas.drawText(poiName, -xCenter + 5, -yCenter + 20, pName);
				canvas.restore();
			}
		}
	}

}
