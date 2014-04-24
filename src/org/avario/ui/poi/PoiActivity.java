package org.avario.ui.poi;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.PoiProducer;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.poi.POI;
import org.avario.utils.Logger;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PoiActivity extends Activity {

	private EditText nameEdit;
	private EditText latEdit;
	private EditText longEdit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.poi);
		super.onCreate(savedInstanceState);

		nameEdit = (EditText) this.findViewById(R.id.poiName);
		latEdit = (EditText) this.findViewById(R.id.poiLat);
		longEdit = (EditText) this.findViewById(R.id.poiLong);

		loadDefaults();

		Button cancel = (Button) findViewById(R.id.buttonCancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}

		});
		Button save = (Button) findViewById(R.id.buttonSave);
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (nameEdit.getText().toString().equals("") || latEdit.getText().toString().equals("")
						|| longEdit.getText().toString().equals("")) {
					return;
				}
				String poiName = nameEdit.getText().toString();
				Double poiLat = Double.valueOf(latEdit.getText().toString());
				Double poiLong = Double.valueOf(longEdit.getText().toString());
				try {
					PackageInfo pInfo = AVarioActivity.CONTEXT.getPackageManager().getPackageInfo(
							AVarioActivity.CONTEXT.getPackageName(), 0);
					PoiProducer.get().notify(new POI(poiName, poiLat, poiLong, pInfo.versionCode));
				} catch (NameNotFoundException e) {
					Logger.get().log("Fail to record POI ", e);
				} finally {
					finish();
				}

			}

		});
	}

	protected void loadDefaults() {
		if (DataAccessObject.get().getLastlocation() != null) {
			latEdit.setText("" + DataAccessObject.get().getLastlocation().getLatitude());
			longEdit.setText("" + DataAccessObject.get().getLastlocation().getLongitude());
		}
	}
}
