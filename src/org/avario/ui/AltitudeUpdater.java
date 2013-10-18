package org.avario.ui;

import java.util.ArrayDeque;

import org.avario.engine.DataAccessObject;
import org.avario.ui.view.AltitudeView;
import org.avario.utils.Logger;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;

public class AltitudeUpdater extends AsyncTask<Object, Object, Object> {
	private AltitudeView altView;
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

	private ArrayDeque<Float> varioSpeed;
	private int maxAltitudeCount = 0;

	public AltitudeUpdater(AltitudeView altView) {
		super();
		this.altView = altView;
	}

	public void drawAltitudes(Canvas canvas) {
		canvas.save();
		if (maxAltitudeCount == 0) {
			maxAltitudeCount = Math.round(canvas.getWidth() / 12);
			varioSpeed = new ArrayDeque<Float>(maxAltitudeCount);
		}

		int height = canvas.getHeight();
		float middle = height / 2f;
		int offset = canvas.getWidth();
		for (Float vSpeed : varioSpeed) {
			float alt = middle - vSpeed * middle / maxAplitude;
			Paint paint = vSpeed < 0 ? downPaint : upPaint;
			canvas.drawLine(offset, middle, offset, alt, paint);
			offset -= 12;
		}
		// canvas.restore();
	}

	@Override
	protected Object doInBackground(Object... arg0) {
		boolean cancel = false;
		while (!cancel) {
			float vSpeed = DataAccessObject.get().getLastVSpeed();
			if (maxAplitude < Math.abs(vSpeed)) {
				varioSpeed.push(vSpeed < 0 ? -maxAplitude : maxAplitude);
			} else {
				varioSpeed.push(vSpeed);
			}
			publishProgress();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
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
