package org.avario.engine.sensors.bt;

import org.avario.engine.SensorProducer;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.sensors.CompasSensorThread;
import org.avario.engine.sensors.LocationThread;

import android.app.Activity;

public class OutsideProducer extends SensorProducer {
	private static OutsideProducer THIS;

	protected OutsideProducer() {
		super();
	}

	public static void init(Activity activity) {
		THIS = new OutsideProducer();
		THIS.initSensorsListners(activity);
	}

	private void initSensorsListners(Activity activity) {
		compasThread = new CompasSensorThread();
		locationThread = new LocationThread();

		compasThread.startSensor();
		locationThread.startSensor();

		DataAccessObject.get().setMovementFactor(new OutsideFactor());
	}

	public static OutsideProducer get() {
		return THIS;
	}

}
