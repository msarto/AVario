package org.avario.utils.bt.le.services.flytech;

import java.util.UUID;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.utils.bt.le.services.CharacteristicHandler;
import org.avario.utils.bt.le.services.ServiceHandler;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.location.Location;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SensorServiceHandler extends ServiceHandler {
	// UUID: <aba27100-143b4b81-a444edcd-0000f020>
	private volatile static Location lastlocation = new Location("Sensbox");

	static {
		// characteristics.put(UUID.fromString("aba27100-143b-4b81-a444-edcd0000f021"),
		// new MovementCharacteristic());
		characteristics.put(UUID.fromString("aba27100-143b-4b81-a444-edcd0000f022"), new NavigationCharacteristic());
		characteristics.put(UUID.fromString("aba27100-143b-4b81-a444-edcd0000f023"), new MovementCharacteristic());
		characteristics.put(UUID.fromString("aba27100-143b-4b81-a444-edcd0000f024"), new GPSSecondaryCharacteristic());
		characteristics.put(UUID.fromString("aba27100-143b-4b81-a444-edcd0000f025"), new StatusCharacteristic());
	}

	@Override
	public String getServiceId() {
		return "aba27100-143b4b81-a444edcd-0000f020";
	}

	public static class MovementCharacteristic implements CharacteristicHandler {
		// UUID aba27100-143b-4b81-a444-edcd0000f023
		@Override
		public void handleCharacteristic(BluetoothGattCharacteristic characteristic) {
			long presureAlt = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0);
			int cms = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4);
			int dmsSpeed = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 6);
			// int heading =
			// characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,
			// 8);

			DataAccessObject.get().getMovementFactor().notify(0, (float) (cms * 0.01f));
			DataAccessObject.get().setLastAltitude(presureAlt * 0.01f);
			lastlocation.setSpeed(dmsSpeed * 0.1f);
			lastlocation.setAltitude(presureAlt);
		}

		@Override
		public long getLatency() {
			return 100;
		}
	}

	public static class NavigationCharacteristic implements CharacteristicHandler {
		// UUID aba27100-143b-4b81-a444-edcd0000f022
		@Override
		public void handleCharacteristic(BluetoothGattCharacteristic characteristic) {
			long dateTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
			long latitude = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 4);
			long longitude = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 8);
			int gpsAltitude = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 12);
			lastlocation.setTime(dateTime);
			lastlocation.setLatitude(latitude * 0.0000001f);
			lastlocation.setLongitude(longitude * 0.0000001f);
			lastlocation.setAltitude(gpsAltitude);
			DataAccessObject.get().setLastlocation(lastlocation);
		}

		@Override
		public long getLatency() {
			return 1000;
		}
	}

	public static class GPSSecondaryCharacteristic implements CharacteristicHandler {
		// UUID aba27100-143b-4b81-a444-edcd0000f024
		@Override
		public void handleCharacteristic(BluetoothGattCharacteristic characteristic) {
			int accuracy = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
			lastlocation.setAccuracy(accuracy * 0.1f);
		}

		@Override
		public long getLatency() {
			return 1000;
		}
	}

	public static class StatusCharacteristic implements CharacteristicHandler {
		// UUID aba27100-143b-4b81-a444-edcd0000f024
		@Override
		public void handleCharacteristic(BluetoothGattCharacteristic characteristic) {
			int temperature = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 6);
			DataAccessObject.get().setTemperature(temperature * 0.1f);
		}

		@Override
		public long getLatency() {
			return 5000;
		}
	}
}
