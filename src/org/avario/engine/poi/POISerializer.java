package org.avario.engine.poi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class POISerializer {
	private static final POISerializer THIS = new POISerializer();

	protected POISerializer() {

	}

	public static POISerializer get() {
		return THIS;
	}

	public POI read(final ObjectInputStream poiStream, int version) throws IOException {
		String poiName = poiStream.readUTF();
		double poiLat = poiStream.readDouble();
		double poiLong = poiStream.readDouble();
		return new POI(poiName, poiLat, poiLong, version);
	}

	public void write(POI poi, final ObjectOutputStream poiStream) throws IOException {		
		poiStream.writeInt(poi.getVersion());
		poiStream.writeUTF(poi.getName());
		poiStream.writeDouble(poi.getLatitude());
		poiStream.writeDouble(poi.getLongitude());
	}
}
