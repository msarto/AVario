package org.avario.utils.bt.le;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.avario.AVarioActivity;
import org.avario.engine.sensors.bt.OutsideProducer;
import org.avario.utils.Logger;
import org.avario.utils.bt.le.services.CharacteristicHandler;
import org.avario.utils.bt.le.services.ServiceHandler;
import org.avario.utils.bt.le.services.flytech.SensorServiceHandler;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class LEBTCallback extends BluetoothGattCallback {
	private static final CountDownLatch semaphore = new CountDownLatch(1);
	private List<BluetoothGattService> leServices = new ArrayList<BluetoothGattService>();

	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
		leServices = gatt.getServices();
		semaphore.countDown();
		for (BluetoothGattService service : leServices) {
			ServiceHandler serviceHandler = getHandle(gatt, service.getUuid());
			if (serviceHandler != null) {
				Logger.get().log("Handle Service UUID" + service.getUuid().toString());
				serviceHandler.handleService(gatt, service);
			} else {
				Logger.get().log("Unknown Service UUID" + service.getUuid().toString());
			}
		}
	}

	protected ServiceHandler getHandle(BluetoothGatt gatt, final UUID uuid) {
		if ("aba27100-143b-4b81-a444-edcd0000f020".equals(uuid.toString())) {
			return new SensorServiceHandler();
		}
		return null;
	}

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		CharacteristicHandler characteristicHandler = ServiceHandler.getCharacteristicHandler(characteristic.getUuid());
		if (characteristicHandler != null) {
			characteristicHandler.handleCharacteristic(characteristic);
		}
	}

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		CharacteristicHandler characteristicHandler = ServiceHandler.getCharacteristicHandler(characteristic.getUuid());
		if (characteristicHandler != null) {
			characteristicHandler.handleCharacteristic(characteristic);
		}
	}

	public void discover(final BluetoothGatt gatt) throws InterruptedException {
		semaphore.await(1, TimeUnit.SECONDS);
		gatt.connect();
		gatt.discoverServices();
		if (leServices.size() == 0) {
			leServices = gatt.getServices();
		}
		semaphore.await(2, TimeUnit.SECONDS);
		OutsideProducer.init(AVarioActivity.CONTEXT);
	}
}
