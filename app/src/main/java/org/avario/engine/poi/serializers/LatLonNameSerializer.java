package org.avario.engine.poi.serializers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.avario.engine.poi.POI;
import org.avario.engine.poi.Serializer;

public class LatLonNameSerializer implements Serializer {

	@Override
	public POI read(final ObjectInputStream poiStream, int version)
			throws IOException {
		String poiName = poiStream.readUTF();
		double poiLat = poiStream.readDouble();
		double poiLong = poiStream.readDouble();
		return new POI(poiName, poiLat, poiLong, 0, version);
	}

	@Override
	public void write(POI poi, final ObjectOutputStream poiStream)
			throws IOException {
		poiStream.writeInt(poi.getVersion());
		poiStream.writeUTF(poi.getName());
		poiStream.writeDouble(poi.getLatitude());
		poiStream.writeDouble(poi.getLongitude());
	}

}
