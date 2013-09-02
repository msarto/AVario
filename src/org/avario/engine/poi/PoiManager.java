package org.avario.engine.poi;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.avario.utils.Logger;

import android.os.Environment;

public class PoiManager {
	private static final int MAX_POIS = 100;
	private static PoiManager THIS;
	private final Map<String, POI> pois = new HashMap<String, POI>();
	private final Map<String, POI> webpois = new HashMap<String, POI>();
	private POI activePOI;

	protected PoiManager() {
	}

	public static void init() {
		if (THIS == null) {
			THIS = new PoiManager();
			THIS.readPOIs();
		}
	}

	public static PoiManager get() {
		init();
		return THIS;
	}

	public Collection<POI> getPOIs() {
		synchronized (THIS) {
			List<POI> values = new ArrayList<POI>();
			values.addAll(pois.values());
			values.addAll(webpois.values());
			Collections.sort(values, new Comparator<POI>() {

				@Override
				public int compare(POI lhs, POI rhs) {
					return lhs.getName().compareTo(rhs.getName());
				}
			});
			return values;
		}
	}

	public void addPOI(POI poi) {
		pois.put(poi.getName(), poi);
		writePOIs();
	}

	public void addWebPOI(POI poi) {
		webpois.put(poi.getName(), poi);
		writePOIs();
	}

	public void delPOI(String name) {
		pois.remove(name);
		writePOIs();
	}

	public int readPOIs() {
		synchronized (THIS) {
			pois.clear();
			File poiFile = new File(Environment.getExternalStorageDirectory() + File.separator + "AVario" + File.separator + "pois");
			if (!poiFile.exists()) {
				Logger.get().log("No poi file available...");
				return 0;
			}
			ObjectInputStream poiStream = null;
			String poiName = "";
			int poisCount = 0;
			try {
				poiStream = new ObjectInputStream(new FileInputStream(poiFile));
				while (poisCount++ < MAX_POIS) {
					poiName = poiStream.readUTF();
					double poiLat = poiStream.readDouble();
					double poiLong = poiStream.readDouble();
					pois.put(poiName, new POI(poiName, poiLat, poiLong));
				}
			} catch (Exception e) {
				activePOI = pois.get(poiName);
				Logger.get().log("Pois deserailized: " + poisCount + ". Active poi: " + poiName);
			} finally {
				close(poiStream);
			}

			return pois.size();
		}
	}

	public synchronized int writePOIs() {
		synchronized (THIS) {
			File poiFile = new File(Environment.getExternalStorageDirectory() + File.separator + "AVario" + File.separator + "pois");
			ObjectOutputStream poiStream = null;
			try {
				int poiSerialized = 0;
				poiStream = new ObjectOutputStream(new FileOutputStream(poiFile, false));
				for (POI poi : pois.values()) {
					poiStream.writeUTF(poi.getName());
					poiStream.writeDouble(poi.getLatitude());
					poiStream.writeDouble(poi.getLongitude());
					poiSerialized++;
				}
				String poiActiveName = activePOI != null ? activePOI.getName() : "";
				poiStream.writeUTF(poiActiveName);

				Logger.get().log("Pois serailized: " + poiSerialized);
			} catch (Exception e) {
				Logger.get().log("Poi write error ", e);
			} finally {
				close(poiStream);
			}

			return pois.size();
		}
	}

	public POI getActivePOI() {
		return activePOI;
	}

	public void setActivePOI(POI activePOI) {
		this.activePOI = activePOI;
		if (!pois.containsKey(activePOI.getName())) {
			pois.put(activePOI.getName(), activePOI);
		}
		writePOIs();
	}

	private void close(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				Logger.get().log("Fail closing stream ", e);
			}
		}
	}

}
