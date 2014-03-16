package org.avario.utils.bt;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.utils.Logger;
import org.avario.utils.bt.le.LEBTAdapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BTScanner {

	private static final BTScanner THIS = new BTScanner();
	public static final int INTENT_ID = 901;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean leBt = false;

	protected BTScanner() {
	}

	public static BTScanner get() {
		return THIS;
	}

	public boolean getLeBt(){
		return leBt;
	}

	public synchronized boolean scan() {
		try {
			leBt = AVarioActivity.CONTEXT.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
			if (!leBt) {
				Toast.makeText(AVarioActivity.CONTEXT, R.string.sensbox_not_supported, Toast.LENGTH_LONG).show();
				return false;
			}
			getAddapter();
			if (!mBluetoothAdapter.isEnabled()) {
				AVarioActivity.CONTEXT.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
						INTENT_ID);
			} else {
				proceedWithBTDevice();
			}
			this.wait(5000);
		} catch (Exception ex) {
			Toast.makeText(AVarioActivity.CONTEXT, R.string.sensbox_not_supported, Toast.LENGTH_SHORT).show();
		}
		return leBt;
	}

	protected void proceedWithBTDevice() {

		if (leBt) {
			LEBTAdapter.get().scanLeDevice(mBluetoothAdapter, true);
		} else {
			if (mBluetoothAdapter.isDiscovering()) {
				mBluetoothAdapter.cancelDiscovery();
			}
			mBluetoothAdapter.startDiscovery();
			AVarioActivity.CONTEXT.registerReceiver(new BroadcastReceiver() {
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if (BluetoothDevice.ACTION_FOUND.equals(action)) {
						BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						Logger.get().log("Found: " + device.getName());
					}
					BTScanner.this.btDone();
				}
			}, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		}
	}

	public synchronized void btDone() {
		this.notify();
		
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTENT_ID && data != null && resultCode == Activity.RESULT_OK) {
			proceedWithBTDevice();
		}
	}

	public void clear() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		if (leBt) {
			LEBTAdapter.get().clear();
		}
	}

	private BluetoothAdapter getAddapter() {
		if (mBluetoothAdapter != null) {
			return mBluetoothAdapter;
		}
		try {
			Object bt = AVarioActivity.CONTEXT.getSystemService(/*
																 * Context.
																 * BLUETOOTH_SERVICE
																 */"bluetooth");
			if (bt != null && bt instanceof BluetoothManager) {
				mBluetoothAdapter = ((BluetoothManager) bt).getAdapter();
				return mBluetoothAdapter;
			}
		} catch (Exception ex) {
			Logger.get().log("Unsupported LE BT at this API");
		}
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		return mBluetoothAdapter;
	}
}
