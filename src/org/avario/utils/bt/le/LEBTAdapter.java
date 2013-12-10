package org.avario.utils.bt.le;

import org.avario.AVarioActivity;
import org.avario.utils.Logger;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class LEBTAdapter {

	private static final LEBTAdapter THIS = new LEBTAdapter();
	private boolean mScanning;
	private boolean isConnected = false;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice leDevice;
	private LEBTCallback handler;
	private BluetoothGatt gatt;

	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;

	protected LEBTAdapter() {

	}

	public static LEBTAdapter get() {
		return THIS;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void scanLeDevice(final BluetoothAdapter bluetoothAdapter, final boolean enable) {
		this.mBluetoothAdapter = bluetoothAdapter;
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

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			if (mScanning) {
				leDevice = device;
				isConnected = true;
				stopScanning();
			}
		}
	};

	private void startLEDeviceandle(BluetoothDevice leDevice) {
		if (leDevice != null) {
			if (handler == null) {
				handler = new LEBTCallback();
				gatt = leDevice.connectGatt(AVarioActivity.CONTEXT, true, handler);
				try {
					handler.discover(gatt);
				} catch (InterruptedException e) {
					Logger.get().log("Unsupported LE BT at this API");
				}
			}
			// gatt.disconnect();
		}
	}

	public void clear() {
		isConnected = false;
		if (gatt != null) {
			gatt.close();
			gatt = null;
		}

	}
}
