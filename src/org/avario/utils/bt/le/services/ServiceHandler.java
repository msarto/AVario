package org.avario.utils.bt.le.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.avario.utils.Logger;
import org.avario.utils.bt.le.LEBTAdapter;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class ServiceHandler {
	protected static final Map<UUID, CharacteristicHandler> characteristics = new HashMap<UUID, CharacteristicHandler>();

	public abstract String getServiceId();

	public static CharacteristicHandler getCharacteristicHandler(UUID uuid) {
		return characteristics.get(uuid);
	}

	public void handleService(final BluetoothGatt gatt, final BluetoothGattService service) {
		List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
		for (final BluetoothGattCharacteristic characteristic : characteristics) {
			final CharacteristicHandler cHandler = getCharacteristicHandler(characteristic.getUuid());
			if (cHandler != null) {
				// gatt.setCharacteristicNotification(characteristic, true);
				// characteristic.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				// gatt.writeCharacteristic(characteristic);
				new Thread(new Runnable() {

					@Override
					public void run() {
						while (LEBTAdapter.get().isConnected()) {
							gatt.readCharacteristic(characteristic);
							try {
								Thread.sleep(cHandler.getLatency());
							} catch (InterruptedException e) {
								break;
							}
						}
					}
				}).start();
				// gatt.readCharacteristic(characteristic);
				// for (BluetoothGattDescriptor descriptor :
				// characteristic.getDescriptors()) {
				// descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				// gatt.writeDescriptor(descriptor);
				// }
			} else {
				Logger.get().log("Unknown characteristic UUID: " + characteristic.getUuid().toString());
			}
		}

	}
}
