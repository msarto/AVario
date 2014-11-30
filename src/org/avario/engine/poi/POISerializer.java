package org.avario.engine.poi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.avario.engine.poi.serializers.LatLonAltNameSerializer;
import org.avario.engine.poi.serializers.LatLonNameSerializer;

import android.util.SparseArray;

public class POISerializer {
	private static final POISerializer THIS = new POISerializer();
	private final SparseArray<Serializer> serializers = new SparseArray<Serializer>();

	protected POISerializer() {
		serializers.put(1, new LatLonNameSerializer());
		serializers.put(33, new LatLonAltNameSerializer());
	}

	public static POISerializer get() {
		return THIS;
	}

	public POI readPOI(final ObjectInputStream poiStream, int version)
			throws IOException {
		Serializer serailizer = getSerializer(version);
		if (serailizer != null) {
			return serailizer.read(poiStream, version);
		}
		return null;
	}

	public void writePOI(POI poi, final ObjectOutputStream poiStream)
			throws IOException {
		Serializer serailizer = getSerializer(poi.getVersion());
		if (serailizer != null) {
			serailizer.write(poi, poiStream);
		}
	}

	private Serializer getSerializer(int version) {
		for (int i = version; i > 0; i--) {
			Serializer serializer = serializers.get(i);
			if (serializer != null) {
				return serializers.get(i);
			}
		}
		return null;
	}
}
