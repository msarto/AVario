package org.avario.inappbilling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.inappbilling.util.IabException;
import org.avario.inappbilling.util.IabHelper;
import org.avario.inappbilling.util.IabHelper.OnConsumeFinishedListener;
import org.avario.inappbilling.util.IabResult;
import org.avario.inappbilling.util.Inventory;
import org.avario.inappbilling.util.Purchase;
import org.avario.inappbilling.util.SkuDetails;
import org.avario.utils.Constants;
import org.avario.utils.Logger;

import android.content.Intent;
import android.widget.Toast;

public class Donate {
	public static final int ACTIVITY_ID = 1001;
	private static Donate THIS = new Donate();
	private IabHelper mHelper;
	protected Map<String, SkuDetails> items = new HashMap<String, SkuDetails>();

	protected Donate() {
	}

	public static Donate get() {
		return THIS;
	}

	public Map<String, SkuDetails> getItems() {
		return items;
	}

	public boolean init() throws Exception {
		Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		serviceIntent.setPackage(AVarioActivity.CONTEXT.getPackageName());
		mHelper = new IabHelper(AVarioActivity.CONTEXT, Constants.KEY);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					Logger.get().log("Donate is not working ... for now");
				} else {
					Toast.makeText(AVarioActivity.CONTEXT, R.string.dont_forget_to_donate, Toast.LENGTH_LONG).show();
					try {
						items = goodies();
						for (Entry<String, SkuDetails> sku : items.entrySet()) {
							Logger.get().log(
									"Sku: " + sku.getKey() + " detauls: " + sku.getValue().getDescription()
											+ "; price: " + sku.getValue().getPrice());
						}
					} catch (Exception e) {
						Logger.get().log("Fail to get donations list", e);
					}
				}
			}
		});
		return true;
	}

	protected Map<String, SkuDetails> goodies() throws IabException {
		List<String> more = new ArrayList<String>();
		more.addAll(Arrays.asList(Constants.SKUS));
		Inventory inv = mHelper.queryInventory(true, more);
		Map<String, Purchase> purchases = inv.getPurchases();
		// if the user purchased a donation we will consume it
		for (Entry<String, Purchase> purchase : purchases.entrySet()) {
			// consume the donation
			try {
				Logger.get().log("Purchase: " + purchase.getKey() + " detauls: " + purchase.getValue().getOrderId());
				mHelper.consumeAsync(purchase.getValue(), new OnConsumeFinishedListener() {

					@Override
					public void onConsumeFinished(Purchase purchase, IabResult result) {
						Logger.get().log("Consumed (" + result.getMessage() + ") - " + purchase);
					}
				});
			} catch (Exception e) {
				Logger.get().log("Fail consuming (" + purchase.getKey() + ") ", e);
			}
		}
		return inv.getSkus();
	}

	public void donate(final String sku) {
		if (mHelper != null) {
			try {
				mHelper.launchPurchaseFlow(AVarioActivity.CONTEXT, sku, ACTIVITY_ID,
						new IabHelper.OnIabPurchaseFinishedListener() {
							public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
								Logger.get().log("Purchase finished: " + result + ", purchase: " + purchase);
								// if we were disposed of in the meantime, quit.
								if (mHelper == null || result.isFailure()) {
									Logger.get().log(
											"Donation failed (" + result.getResponse() + ") " + result.getMessage());
									return;
								} else if (purchase.getSku().equals(sku)) {
									Toast.makeText(AVarioActivity.CONTEXT, R.string.thanks, Toast.LENGTH_LONG).show();
								}
							}
						});
			} catch (Exception e) {
				Logger.get().log("Can not donate " + sku, e);
			}
		}
	}

	public void clear() {
		if (mHelper != null) {
			mHelper.dispose();
			mHelper = null;
		}
	}

	public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
		if (mHelper != null) {
			return mHelper.handleActivityResult(requestCode, resultCode, data);
		}
		return false;
	}
}
