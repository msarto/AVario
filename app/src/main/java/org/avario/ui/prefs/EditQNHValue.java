package org.avario.ui.prefs;

import org.avario.R;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.utils.StringFormatter;

import android.content.Context;
import android.hardware.SensorManager;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class EditQNHValue extends EditTextPreference {

	protected TextView mValueText;

	public EditQNHValue(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.preference_with_value);
	}

	public EditQNHValue(Context context) {
		super(context);
		setLayoutResource(R.layout.preference_with_value);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mValueText = (TextView) view.findViewById(R.id.preference_value);
		if (mValueText != null) {
			mValueText.setText(getText());
		}
		this.setDialogMessage(R.string.ref_alt_description);
		this.setDialogTitle(R.string.ref_alt_title);
	}

	@Override
	public void setText(String text) {
		if (mValueText != null) {
			try {
				int newAltitude = Integer.valueOf(text);
				text = computeQNHFromAltitude(newAltitude);
				mValueText.setText(text);
			} catch (Exception e) {
				super.setText("1013.25");
			}
		}
		super.setText(text);

	}

	private String computeQNHFromAltitude(int newAltitude) {
		float ref = SensorManager.PRESSURE_STANDARD_ATMOSPHERE + 3000;
		// double h = location.getAltitude();
		// adjust the reference pressure until the pressure sensor
		// altitude match the gps altitude +-2m
		float lastPresure = DataAccessObject.get().getLastPresure();
		if (lastPresure > 0 && newAltitude > 0 && newAltitude < 10000) {
			double delta = Math.abs(SensorManager.getAltitude(ref, lastPresure) - newAltitude);
			while (delta > 2 && ref > 0) {
				ref -= 0.1 * delta;
				delta = Math.abs(SensorManager.getAltitude(ref, lastPresure) - newAltitude);
			}
		} else {
			ref = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
		}
		return String.valueOf(ref);
	}

	@Override
	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		// TODO Auto-generated method stub
		editText.setText(StringFormatter.noDecimals(DataAccessObject.get().getLastAltitude()));
		super.onAddEditTextToDialogView(dialogView, editText);
	}
}
