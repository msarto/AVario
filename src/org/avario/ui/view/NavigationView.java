package org.avario.ui.view;

import org.avario.ui.NavigatorUpdater;
import org.avario.utils.Logger;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class NavigationView extends LinearLayout {

	public NavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NavigationView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			int hScroll = computeHorizontalScrollRange();
			int vScroll = computeVerticalScrollRange();
			LayoutParams params = (LayoutParams) getLayoutParams();
			// Changes the height
			params.height = hScroll;
			try {
				NavigatorUpdater.get().draw(canvas, hScroll / 2, vScroll / 2);
			} catch (Exception ex) {
				Logger.get().log("Fail updating navigation ui", ex);
			}
		} catch (Exception ex) {
			Logger.get().log("Fail to draw navigation ...", ex);
		} finally {
			super.onDraw(canvas);
		}
	}
}
