package org.avario.utils.bt.le;

import java.util.List;

import org.avario.utils.Logger;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;

@SuppressLint("NewApi")
public class LEBTCallback extends BluetoothGattCallback {
	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
		List<BluetoothGattService> services = gatt.getServices();
		for (BluetoothGattService service : services) {
			Logger.get().log("Service UUID" + service.getUuid().toString());
		}
	}

	public void handle(BluetoothGatt gatt) {
		gatt.connect();
		gatt.discoverServices();
		//while (true) {
		//	try {
		//		Thread.sleep(10);
		//	} catch (InterruptedException e) {
		//		// TODO Auto-generated catch block
		//		e.printStackTrace();
		//	}
		//}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
