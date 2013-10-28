package org.avario.ui;

import java.util.ArrayDeque;

import org.avario.engine.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.ui.view.AltitudeView;
import org.avario.utils.Logger;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;

public class AltitudeUpdater extends AsyncTask<Object, Object, Object> {
	private AltitudeView altView;
	private final int height;
	private final int width;
	private final int lineWidth;
	private static final float maxAplitude = 2;
	private static final Paint upPaint = new Paint();
	private static final Paint downPaint = new Paint();
	static {
		upPaint.setColor(Color.GREEN);
		upPaint.setStyle(Paint.Style.STROKE);
		upPaint.setStrokeWidth(10);
		upPaint.setAntiAlias(true);

		downPaint.setColor(Color.RED);
		downPaint.setStyle(Paint.Style.STROKE);
		downPaint.setStrokeWidth(10);
		downPaint.setAntiAlias(true);
	}

	private volatile ArrayDeque<Float> varioSpeed;
	private int maxAltitudeCount = 0;

	public AltitudeUpdater(AltitudeView altView, int height, int width, float densityMultiplier) {
		super();
		this.height = height;
		this.width = width;
		this.altView = altView;
		this.lineWidth = Math.round(densityMultiplier * 10);
		upPaint.setStrokeWidth(lineWidth);
		downPaint.setStrokeWidth(lineWidth);
	}

	public synchronized void drawAltitudes(Canvas canvas) {
		canvas.save();
		try {
			if (maxAltitudeCount == 0) {
				maxAltitudeCount = Math.round(canvas.getWidth() / (lineWidth + 2));
				varioSpeed = new ArrayDeque<Float>(maxAltitudeCount);
			}

			float middle = height / 2f;
			int offset = width;
			for (Float vSpeed : varioSpeed) {
				float alt = middle - vSpeed * middle / maxAplitude;
				Paint paint = vSpeed < 0 ? downPaint : upPaint;
				canvas.drawLine(offset, middle, offset, alt, paint);
				offset -= (lineWidth + 2);
			}
		} catch (Exception ex) {
			Logger.get().log("Fail drawing altitudes ", ex);
		} finally {
			canvas.restore();
		}
	}

	@Override
	protected Object doInBackground(Object... arg0) {
		boolean cancel = false;

		while (!cancel) {
			try {
				float vSpeed = DataAccessObject.get().getLastVSpeed();
				if (varioSpeed.size() >= maxAltitudeCount) {
					varioSpeed.pollLast();
				}
				if (maxAplitude < Math.abs(vSpeed)) {
					varioSpeed.push(vSpeed < 0 ? -maxAplitude : maxAplitude);
				} else {
					varioSpeed.push(vSpeed);
				}
				publishProgress();
				try {
					Thread.sleep(100 + Preferences.beep_interval);
				} catch (InterruptedException e) {
					cancel = true;
				}
			} catch (Exception e) {
				cancel = true;
			}
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(final Object... params) {
		try {
			altView.invalidate();
		} catch (Exception ex) {
			Logger.get().log("Fail async beep progress: ", ex);
		}
	}

}
