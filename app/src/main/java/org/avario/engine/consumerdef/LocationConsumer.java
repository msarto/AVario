package org.avario.engine.consumerdef;

import android.location.Location;

public interface LocationConsumer extends VarioConsumer {
	void notifyWithLocation(Location location);
}
