package org.avario.engine;

import java.util.ArrayList;
import java.util.List;

import org.avario.R;
import org.avario.engine.consumerdef.AccelerometerConsumer;
import org.avario.engine.consumerdef.BarometerConsumer;
import org.avario.engine.consumerdef.CompasConsumer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.consumerdef.VarioConsumer;
import org.avario.engine.sensors.BaroSensorThread;
import org.avario.engine.sensors.CompasSensorThread;
import org.avario.engine.sensors.LocationThread;
import org.avario.utils.Logger;

import android.app.Activity;
import android.location.Location;
import android.widget.Toast;

public class SensorProducer {
	private static SensorProducer THIS;

	private final List<VarioConsumer> gspConsumers = new ArrayList<VarioConsumer>();
	private final List<VarioConsumer> baroConsumers = new ArrayList<VarioConsumer>();
	private final List<VarioConsumer> compasConsumers = new ArrayList<VarioConsumer>();
	private final List<VarioConsumer> accelerometerConsumers = new ArrayList<VarioConsumer>();

	protected CompasSensorThread compasThread;
	protected BaroSensorThread baroThread;
	protected LocationThread locationThread;

	protected SensorProducer() {

	}

	public static void init(Activity activity, boolean useInternalSensors) {
		THIS = new SensorProducer();
		if (useInternalSensors) {
			THIS.initSensorsListners(activity);
		}

	}

	public static SensorProducer get() {
		return THIS;
	}

	public static void clear() {
		THIS.close();
	}

	protected void close() {
		gspConsumers.clear();
		baroConsumers.clear();
		gspConsumers.clear();

		if (compasThread != null) {
			compasThread.stop();
		}
		if (baroThread != null) {
			baroThread.stop();
		}
		if (locationThread != null) {
			locationThread.stop();
		}
	}

	public synchronized void registerConsumer(VarioConsumer consumer) {
		Logger.get().log("Register consumer " + consumer);
		if (consumer instanceof LocationConsumer) {
			gspConsumers.add((LocationConsumer) consumer);
			Logger.get().log("Register GPS consumer " + consumer.getClass().getName());
		}

		if (consumer instanceof BarometerConsumer) {
			baroConsumers.add((BarometerConsumer) consumer);
			Logger.get().log("Register baro consumer " + consumer.getClass().getName());
		}

		if (consumer instanceof CompasConsumer) {
			compasConsumers.add((CompasConsumer) consumer);
			Logger.get().log("Register compas consumer " + consumer.getClass().getName());
		}

		if (consumer instanceof AccelerometerConsumer) {
			accelerometerConsumers.add((AccelerometerConsumer) consumer);
			Logger.get().log("Register accelerometer consumer " + consumer.getClass().getName());
		}
	}

	public synchronized void unregisterConsumer(VarioConsumer consumer) {
		Logger.get().log("UnRegister consumer " + consumer);
		if (consumer instanceof LocationConsumer) {
			Logger.get().log("UnRegister GPS consumer " + consumer.getClass().getName());
			gspConsumers.remove(consumer);
		}

		if (consumer instanceof BarometerConsumer) {
			Logger.get().log("UnRegister baro consumer " + consumer.getClass().getName());
			baroConsumers.remove(consumer);

		}

		if (consumer instanceof CompasConsumer) {
			Logger.get().log("UnRegister compas consumer " + consumer.getClass().getName());
			compasConsumers.remove(consumer);
		}

		if (consumer instanceof AccelerometerConsumer) {
			Logger.get().log("UnRegister accelerometer consumer " + consumer.getClass().getName());
			accelerometerConsumers.remove(consumer);

		}
	}

	private void initSensorsListners(Activity activity) {
		compasThread = new CompasSensorThread();
		baroThread = new BaroSensorThread();
		locationThread = new LocationThread();

		compasThread.startSensor();
		baroThread.startSensor();
		locationThread.startSensor();

		Toast.makeText(activity, R.string.initalizing_sensors, Toast.LENGTH_LONG).show();
	}

	public void notifyGpsConsumers(Location location) {
		synchronized (gspConsumers) {
			for (VarioConsumer consumer : gspConsumers) {
				((LocationConsumer) consumer).notifyWithLocation(location);
			}
		}
	}

	public void notifyBaroConsumers(float altitude) {
		synchronized (baroConsumers) {
			for (VarioConsumer consumer : baroConsumers) {
				((BarometerConsumer) consumer).notifyWithAltFromPreasure(altitude);
			}
		}
	}

	public void notifyCompasConsumers(float bearing) {
		synchronized (compasConsumers) {
			for (VarioConsumer consumer : compasConsumers) {
				((CompasConsumer) consumer).notifyNorth(bearing);
			}
		}
	}

	public void notifyAccelerometerConsumers(float x, float y, float z) {
		synchronized (accelerometerConsumers) {
			for (VarioConsumer consumer : accelerometerConsumers) {
				((AccelerometerConsumer) consumer).notifyAcceleration(x, y, z);
			}
		}
	}

}
