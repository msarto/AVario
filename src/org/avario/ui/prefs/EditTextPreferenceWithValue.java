package org.avario.ui.prefs;

import org.avario.R;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class EditTextPreferenceWithValue extends EditTextPreference {

	private TextView mValueText;

	public EditTextPreferenceWithValue(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.preference_with_value);
	}

	public EditTextPreferenceWithValue(Context context) {
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
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		if (mValueText != null) {
			mValueText.setText(getText());
		}
	}
}
