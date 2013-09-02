package org.avario.engine.consumerdef;

import org.avario.engine.poi.POI;

public interface POIConsumer {
	void notifyWithPOI(POI poi);
}
