package org.avario.ui.prefs;

import org.avario.R;
import org.avario.utils.Logger;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

	private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";

	private int mMaxValue = 100;
	private int mMinValue = 0;
	private int mInterval = 1;
	private int mCurrentValue;
	private String mUnitsLeft = "";
	private String mUnitsRight = "";
	private SeekBar mSeekBar;

	private TextView mStatusText;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPreference(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}

	private void initPreference(Context context, AttributeSet attrs) {
		setValuesFromXml(attrs);
		mSeekBar = new SeekBar(context, attrs);
		mSeekBar.setMax(mMaxValue - mMinValue);
		mSeekBar.setOnSeekBarChangeListener(this);
	}

	private void setValuesFromXml(AttributeSet attrs) {
		mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		RelativeLayout layout = null;
		try {
			LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layout = (RelativeLayout) mInflater.inflate(R.layout.seek_bar_preference, parent, false);
		} catch (Exception e) {
			Logger.get().log("Error creating seek bar preference", e);
		}

		return layout;

	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		try {
			// move our seekbar to the new view we've been given
			ViewParent oldContainer = mSeekBar.getParent();
			ViewGroup newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);

			if (oldContainer != newContainer) {
				// remove the seekbar from the old view
				if (oldContainer != null) {
					((ViewGroup) oldContainer).removeView(mSeekBar);
				}
				// remove the existing seekbar (there may not be one) and add
				// ours
				newContainer.removeAllViews();
				newContainer.addView(mSeekBar, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			}
		} catch (Exception ex) {
			Logger.get().log("Error binding view: " + ex.toString());
		}

		updateView(view);
	}

	/**
	 * Update a SeekBarPreference view with our current state
	 * 
	 * @param view
	 */
	protected void updateView(View view) {

		try {
			RelativeLayout layout = (RelativeLayout) view;

			mStatusText = (TextView) layout.findViewById(R.id.seekBarPrefValue);
			mStatusText.setText(String.valueOf(mCurrentValue));
			mStatusText.setMinimumWidth(30);

			mSeekBar.setProgress(mCurrentValue - mMinValue);

			TextView unitsRight = (TextView) layout.findViewById(R.id.seekBarPrefUnitsRight);
			unitsRight.setText(mUnitsRight);

			TextView unitsLeft = (TextView) layout.findViewById(R.id.seekBarPrefUnitsLeft);
			unitsLeft.setText(mUnitsLeft);

		} catch (Exception e) {
			Logger.get().log("Error updating seek bar preference", e);
		}

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int newValue = progress + mMinValue;

		if (newValue > mMaxValue)
			newValue = mMaxValue;
		else if (newValue < mMinValue)
			newValue = mMinValue;
		else if (mInterval != 1 && newValue % mInterval != 0)
			newValue = Math.round(((float) newValue) / mInterval) * mInterval;

		// change rejected, revert to the previous value
		if (!callChangeListener(newValue)) {
			seekBar.setProgress(mCurrentValue - mMinValue);
			return;
		}

		// change accepted, store it
		mCurrentValue = newValue;
		mStatusText.setText(String.valueOf(newValue));
		persistString(String.valueOf(newValue));

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		notifyChanged();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index) {
		String defaultValue = ta.getString(index);
		//Toast.makeText(context, defaultValue, Toast.LENGTH_SHORT).show();
		return defaultValue;
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if (restoreValue) {
			mCurrentValue = Integer.valueOf(getPersistedString(String.valueOf(mCurrentValue)));
		} else {
			String temp = "50";
			try {
				temp = defaultValue.toString();
			} catch (Exception ex) {
				Logger.get().log("Invalid default value: " + defaultValue.toString());
			}
			Logger.get().log("Initial: " + temp);
			persistString(temp);
			mCurrentValue = Integer.valueOf(temp);
		}

	}

}