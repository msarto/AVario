package org.avario.utils.bt.le.services;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public interface CharacteristicHandler {

	void handleCharacteristic(BluetoothGattCharacteristic characteristic);

	long getLatency();
}
