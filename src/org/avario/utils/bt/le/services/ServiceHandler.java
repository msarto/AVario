package org.avario.utils.bt.le.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.avario.utils.Logger;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

public abstract class ServiceHandler {
	protected static final Map<UUID, CharacteristicHandler> characteristics = new HashMap<UUID, CharacteristicHandler>();

	public abstract String getServiceId();

	public static CharacteristicHandler getCharacteristicHandler(UUID uuid) {
		return characteristics.get(uuid);
	}

	public void handleService(final BluetoothGatt gatt, final BluetoothGattService service) {
		List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
		for (BluetoothGattCharacteristic characteristic : characteristics) {
			CharacteristicHandler cHandler = getCharacteristicHandler(characteristic.getUuid());
			if (cHandler != null) {
				gatt.setCharacteristicNotification(characteristic, true);
				gatt.readCharacteristic(characteristic);
			} else {
				Logger.get().log("Unknown characteristic UUID" + characteristic.getUuid().toString());
			}
		}

	}
}
