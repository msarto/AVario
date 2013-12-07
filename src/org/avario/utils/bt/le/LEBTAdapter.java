package org.avario.utils.bt.le;

import org.avario.AVarioActivity;
import org.avario.utils.Logger;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.widget.Toast;

@SuppressLint("NewApi")
public class LEBTAdapter {

	private static final LEBTAdapter THIS = new LEBTAdapter();
	private boolean mScanning;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice leDevice;
	private LEBTCallback handler;

	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;

	protected LEBTAdapter() {

	}

	public static LEBTAdapter get() {
		return THIS;
	}

	public void scanLeDevice(final BluetoothAdapter mBluetoothAdapter, final boolean enable) {
		this.mBluetoothAdapter = mBluetoothAdapter;
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);

			mScanning = true;
			boolean bScan = mBluetoothAdapter.startLeScan(mLeScanCallback);
			Logger.get().log("scan " + bScan);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	private void stopScanning() {
		if (mScanning) {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			AVarioActivity.CONTEXT.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(AVarioActivity.CONTEXT, "Done scanning BT devices", Toast.LENGTH_SHORT).show();
					startLEDeviceandle(leDevice);
				}
			});
		}
	}

	@SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			if (mScanning) {
				leDevice = device;
				stopScanning();
			}

			// AVarioActivity.CONTEXT.runOnUiThread(new Runnable() {
			// @Override
			// public void run() {
			// if (mScanning) {
			// Logger.get().log("Device " + device + " RSSI " + rssi);
			// stopScanning();
			// startLEDeviceandle(device);
			// }
			// }
			// });
		}
	};

	private void startLEDeviceandle(BluetoothDevice leDevice) {
		if (leDevice != null) {
			if (handler == null) {
				handler = new LEBTCallback();
				BluetoothGatt gatt = leDevice.connectGatt(AVarioActivity.CONTEXT, true, handler);
				handler.handle(gatt);
			}
			// gatt.disconnect();
		}
	}
}
