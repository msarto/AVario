package org.avario.engine.datastore;

import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.BarometerConsumer;
import org.avario.utils.Logger;

public class NotificationTask implements Runnable, BarometerConsumer {
	private volatile Thread blinker;
	private Thread thr;
	private volatile float nps = 0;
	private volatile int velocity = 0;

	NotificationTask() {
		thr = new Thread(this);
	}

	public void start() {
		SensorProducer.get().registerConsumer(this);
		thr.start();
	}

	public void stop() {
		blinker = null;
		SensorProducer.get().unregisterConsumer(this);
	}

	public float getNps() {
		return nps;
	}

	@Override
	public void run() {
		try {
			blinker = Thread.currentThread();
			while (blinker == Thread.currentThread()) {
				Thread.sleep(1000);
				nps = velocity;
				velocity = 0;
			}
		} catch (Exception e) {
		} finally {
			Logger.get().log("NotificationTask task terminated");
		}
	}

	@Override
	public void notifyWithAltFromPreasure(float altitude) {
		velocity++;
	}
}
