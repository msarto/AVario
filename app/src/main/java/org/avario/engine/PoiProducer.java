package org.avario.engine;

import java.util.ArrayList;
import java.util.List;

import org.avario.engine.consumerdef.POIConsumer;
import org.avario.engine.poi.POI;

public class PoiProducer {
	private static final PoiProducer THIS = new PoiProducer();
	private final List<POIConsumer> poiConsumers = new ArrayList<POIConsumer>();

	protected PoiProducer() {

	}

	public static PoiProducer get() {
		return THIS;
	}

	public void addConsumer(POIConsumer consumer) {
		poiConsumers.add(consumer);
	}
	
	public void notify(POI poi) {
		for (POIConsumer poiConsumer : poiConsumers) {
			poiConsumer.notifyWithPOI(poi);
		}
	}
}
