package org.avario.engine.poi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface Serializer {
	POI read(final ObjectInputStream poiStream, int version) throws IOException;

	void write(POI poi, final ObjectOutputStream poiStream) throws IOException;
}