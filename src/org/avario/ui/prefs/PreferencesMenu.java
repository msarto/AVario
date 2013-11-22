package org.avario.ui.prefs;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.prefs.Preferences;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesMenu extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		Preferences.update(AVarioActivity.CONTEXT);
		Preferences.updateVersion(this);
	}

	@Override
	public void onBackPressed() {
		Preferences.update(AVarioActivity.CONTEXT);
		super.onBackPressed();
	}

}
