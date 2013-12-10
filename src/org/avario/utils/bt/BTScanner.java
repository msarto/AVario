package org.avario.utils.bt;

import org.avario.AVarioActivity;
import org.avario.utils.Logger;
import org.avario.utils.bt.le.LEBTAdapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.widget.Toast;

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

	public void scan() {
		try {
			if (!AVarioActivity.CONTEXT.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
				Toast.makeText(AVarioActivity.CONTEXT, "SensBox BT device is not supported by your device",
						Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(AVarioActivity.CONTEXT, "SensBox BT device is supported", Toast.LENGTH_SHORT).show();
				leBt = true;
			}

			getAddapter();
			if (!mBluetoothAdapter.isEnabled()) {
				AVarioActivity.CONTEXT.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
						INTENT_ID);
			} else {
				proceedWithBTDevice();
			}
		} catch (Exception ex) {
			Toast.makeText(AVarioActivity.CONTEXT, "SensBox BT device is not supported by your device",
					Toast.LENGTH_SHORT).show();
		}
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
				}
			}, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTENT_ID && data != null && data.getAction().equals(BluetoothAdapter.ACTION_REQUEST_ENABLE)
				&& resultCode == Activity.RESULT_OK) {
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
