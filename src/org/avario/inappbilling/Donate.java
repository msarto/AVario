package org.avario.inappbilling;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.inappbilling.util.IabHelper;
import org.avario.inappbilling.util.IabResult;
import org.avario.utils.Logger;

import android.widget.Toast;

public class Donate {
	private static Donate THIS = new Donate();
	private static final String base64EncodedPublicKey = "za key";

	private IabHelper mHelper;

	protected Donate() {
	}

	public static Donate get() {
		return THIS;
	}

	public boolean init() {
		mHelper = new IabHelper(AVarioActivity.CONTEXT, base64EncodedPublicKey);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					Logger.get().log("Donate is not working ... for now");
				} else {
					Toast.makeText(AVarioActivity.CONTEXT, R.string.dont_forget_to_donate, Toast.LENGTH_LONG).show();
				}
			}
		});
		return false;
	}
}
