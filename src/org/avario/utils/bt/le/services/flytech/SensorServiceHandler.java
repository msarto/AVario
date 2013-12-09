package org.avario.utils.bt.le.services.flytech;

import java.util.UUID;

import org.avario.utils.Logger;
import org.avario.utils.bt.le.services.CharacteristicHandler;
import org.avario.utils.bt.le.services.ServiceHandler;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SensorServiceHandler extends ServiceHandler {
	// UUID: <aba27100-143b4b81-a444edcd-0000f020>

	static {
		characteristics.put(UUID.fromString("aba27100-143b-4b81-a444-edcd0000f023"), new MovementCharacteristic());
	}

	@Override
	public String getServiceId() {
		return "aba27100-143b4b81-a444edcd-0000f020";
	}

	public static class MovementCharacteristic implements CharacteristicHandler {

		@Override
		public void handleCharacteristic(BluetoothGattCharacteristic characteristic) {
			int presureAlt = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
			Logger.get().log("Presure " + presureAlt);
		}
	}
}
