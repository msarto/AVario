package org.avario.ui;

import java.util.ArrayDeque;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.ui.view.AltitudeView;
import org.avario.utils.Logger;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class AltitudeUpdater implements Runnable {
	private static final float maxAplitude = 2;
	private static final Paint upPaint = new Paint();
	private static final Paint downPaint = new Paint();

	private final AltitudeView altView;
	private final int height;
	private final int width;

	private int lineWidth;
	private Thread updater;

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

	public AltitudeUpdater(AltitudeView altView, int height, int width) {
		super();
		this.height = height;
		this.width = width;
		this.altView = altView;

		updater = new Thread(this);
		updater.start();
	}

	public synchronized void drawAltitudes(Canvas canvas) {
		if (varioSpeed.size() > 0) {
			canvas.save();
			try {
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
	}

	@Override
	public void run() {
		boolean cancel = false;
		float densityMultiplier = altView.getContext().getResources().getDisplayMetrics().density;
		this.lineWidth = Math.round(densityMultiplier * 10);
		upPaint.setStrokeWidth(lineWidth);
		downPaint.setStrokeWidth(lineWidth);
		maxAltitudeCount = Math.round(width / (lineWidth + 2));
		varioSpeed = new ArrayDeque<Float>(maxAltitudeCount);
		varioSpeed.clear();

		while (!cancel) {
			try {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					cancel = true;
				}
				float vSpeed = DataAccessObject.get().getLastVSpeed();
				if (varioSpeed.size() >= maxAltitudeCount) {
					varioSpeed.pollLast();
				}
				if (maxAplitude < Math.abs(vSpeed)) {
					varioSpeed.push(vSpeed < 0 ? -maxAplitude : maxAplitude);
				} else {
					varioSpeed.push(vSpeed);
				}
				altView.postInvalidate();
			} catch (Exception e) {
				cancel = true;
			}
		}
	}

	public void cancel() {
		if (updater != null) {
			updater.interrupt();
		}
	}

}
