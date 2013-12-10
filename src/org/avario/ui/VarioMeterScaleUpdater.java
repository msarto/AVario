package org.avario.ui;

import org.avario.R;
import org.avario.engine.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.StringFormatter;
import org.avario.utils.UnitsConverter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class VarioMeterScaleUpdater extends AsyncTask<Integer, Float, Integer> {
	private static VarioMeterScaleUpdater THIS;
	protected final SparseArray<LinearLayout> scaleView = new SparseArray<LinearLayout>();
	protected TextView varioView = null;
	protected int scaleHeight = 6;
	private Activity context;
	private int currentUnitsMark = 0;

	private volatile boolean updatingUI = false;

	protected VarioMeterScaleUpdater(Activity context) {
		this.context = context;
		final Typeface font = StringFormatter.getLargeFont(context.getApplicationContext());

		int i = -20;
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down20));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down19));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down18));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down17));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down16));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down15));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down14));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down13));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down12));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down11));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down10));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down9));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down8));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down7));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down6));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down5));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down4));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down3));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down2));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.down1));
		i++;
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up1));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up2));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up3));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up4));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up5));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up6));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up7));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up8));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up9));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up10));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up11));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up12));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up13));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up14));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up15));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up16));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up17));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up18));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up19));
		scaleView.put(Integer.valueOf(i++), (LinearLayout) context.findViewById(R.id.up20));
		Logger.get().log("Register  " + (i - 1) + " in scale");
		resizeScaleWithDeviceHeight((LinearLayout) context.findViewById(R.id.scaleParent));
		varioView = (TextView) context.findViewById(R.id.vSpeed);
		varioView.setTypeface(font);
	}

	private void resizeScaleWithDeviceHeight(LinearLayout parent) {
		final float density = context.getResources().getDisplayMetrics().density;
		final float screenH = context.getResources().getDisplayMetrics().heightPixels;
		scaleHeight = Math.round((screenH - density * 30 - density * 140) / 40);
		for (int i = -20; i < 21; i++) {
			LinearLayout scaleItem = scaleView.get(i);
			if (scaleItem != null) {
				LayoutParams scaleParams = (LayoutParams) scaleItem.getLayoutParams();
				scaleParams.height = scaleHeight;
				scaleItem.setLayoutParams(scaleParams);
			}
		}
	}

	public static void init(Activity context) {
		THIS = new VarioMeterScaleUpdater(context);
		THIS.execute();
	}

	public static VarioMeterScaleUpdater getInstance() {
		return THIS;
	}

	public static void clear() {
		THIS.cancel(false);
	}

	protected synchronized void updateSpeed(float speed) {
		try {
			updatingUI = true;
			float vSpeed = UnitsConverter.toPreferredVSpeed(speed);
			varioView.setText(Preferences.units_system == 2 ? StringFormatter.noDecimals(vSpeed) : StringFormatter
					.twoDecimals(vSpeed));
			float verticalSpeed = speed > 5 ? 5 : speed;
			verticalSpeed = verticalSpeed < -5 ? -5 : verticalSpeed;
			int unitsMarked = Math.round(4 * verticalSpeed);
			if (currentUnitsMark == unitsMarked) {
				// Nothing to do, just return
				return;
			}
			if (unitsMarked > 0) {
				// Start mark from the bottom
				int marker = -20;
				while (marker <= 20) {
					LinearLayout layout = scaleView.get(marker);
					if (layout != null) {
						if (marker > 0 && unitsMarked >= marker) {
							layout.setBackgroundColor(Color.GREEN);
						} else {
							layout.setBackgroundColor(Color.WHITE);
						}
					}
					marker++;
				}
			} else {
				int marker = 20;
				while (marker >= -20) {
					LinearLayout layout = scaleView.get(marker);
					if (layout != null) {
						if (marker < 0 && unitsMarked <= marker) {
							layout.setBackgroundColor(Color.RED);
						} else {
							layout.setBackgroundColor(Color.WHITE);
						}
					}
					marker--;
				}
			}
			currentUnitsMark = unitsMarked;
		} catch (Exception ex) {
			Logger.get().log("Fil to updte vrio scle with speed: " + speed, ex);
		} finally {
			updatingUI = false;
		}
	}

	@Override
	protected Integer doInBackground(Integer... arg0) {
		while (!THIS.isCancelled()) {
			try {
				Thread.sleep(200);
				float beepSpeed = DataAccessObject.get().getLastVSpeed();
				publishProgress(beepSpeed);
			} catch (InterruptedException e) {
				THIS.cancel(false);
				break;
			} catch (Exception ex) {
				Logger.get().log("Fail in update scale: ", ex);
			}
		}
		Logger.get().log("End Start scale updater ");
		return null;
	}

	@Override
	protected void onProgressUpdate(final Float... speed) {
		try {
			if (!updatingUI) {
				float vSpeed = Math.abs(speed[0]) > Preferences.lift_start ? speed[0] : 0.0f;
				updateSpeed(vSpeed);
			}
		} catch (Exception ex) {
			Logger.get().log("Fail async beep progress: ", ex);
		}
	}
}
