package org.avario.ui.view;

import org.avario.ui.AltitudeUpdater;
import org.avario.utils.Logger;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class AltitudeView extends LinearLayout {

	private AltitudeUpdater viewUpdater;

	public AltitudeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			super.onDraw(canvas);
			if (viewUpdater == null) {
				int hScroll = computeHorizontalScrollRange();
				int vScroll = computeVerticalScrollRange();
				LayoutParams params = (LayoutParams) getLayoutParams();
				// Changes the height
				params.height = vScroll;
				viewUpdater = new AltitudeUpdater(this, vScroll, hScroll);
				Logger.get().log("Init alt updater UI h " + hScroll + " v " + vScroll);
			} else {
				viewUpdater.drawAltitudes(canvas);
			}
		} catch (Exception ex) {
			Logger.get().log("Fail to draw altitude ...", ex);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		if (viewUpdater != null) {
			viewUpdater.cancel();
		}
		super.onDetachedFromWindow();
	}
}
