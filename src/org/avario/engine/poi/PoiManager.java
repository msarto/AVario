package org.avario.engine.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.avario.AVarioActivity;
import org.avario.utils.IOUtils;
import org.avario.utils.Logger;

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
			THIS.reloadPOIS();
		}
	}

	public synchronized void reloadPOIS() {
		pois.clear();
		readPOIs(new File(IOUtils.getStorageDirectory(), "pois"));
		readPOIsV2(new File(IOUtils.getStorageDirectory(), "poisV2"));
	}

	public static PoiManager get() {
		init();
		return THIS;
	}

	public synchronized Collection<POI> getPOIs() {
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

	private int readPOIs(File poiFile) {
		ObjectInputStream poiStream = null;
		String poiName = "";
		int poisCount = 0;
		try {
			if (!poiFile.exists() || !poiFile.canRead()) {
				Logger.get().log("No poi 1 file available...");
				return 0;
			}
			poiStream = new ObjectInputStream(new FileInputStream(poiFile));
			while (poisCount++ < MAX_POIS) {
				poiName = poiStream.readUTF();
				double poiLat = poiStream.readDouble();
				double poiLong = poiStream.readDouble();
				pois.put(poiName, new POI(poiName, poiLat, poiLong, 0, 0));
			}
			activePOI = pois.get(poiName);
		} catch (Throwable e) {
			Logger.get().log("Pois deserailized: " + poisCount + ". Active poi: " + poiName);
		} finally {
			IOUtils.close(poiStream);
		}

		return pois.size();
	}

	private int readPOIsV2(File poiFile) {
		ObjectInputStream poiStream = null;
		String poiName = "";
		int poisCount = 0;
		try {
			if (!poiFile.exists() || !poiFile.canRead()) {
				Logger.get().log("No poi 2 file available on internal storage");
				return 0;
			}

			Logger.get().log("Read poi 2 file available...");
			poiStream = new ObjectInputStream(new FileInputStream(poiFile));
			int poiCount = poiStream.readInt();
			if (poiCount > 0) {
				for (int i = 0; i < poiCount; i++) {
					int version = poiStream.readInt();
					Logger.get().log("Founf poi 2 version " + version);
					POI poi = POISerializer.get().readPOI(poiStream, version);
					pois.put(poi.getName(), poi);
				}
				poiName = poiStream.readUTF();
				activePOI = pois.get(poiName);
			}
		} catch (Exception e) {
			Logger.get().log("Pois deserailized: " + poisCount + ". Active poi: " + poiName);
		} finally {
			IOUtils.close(poiStream);
		}

		return pois.size();
	}

	private synchronized int writePOIs() {
		File poiFile = new File(AVarioActivity.CONTEXT.getFilesDir() + File.separator + "AVario" + File.separator
				+ "poisV2");
		ObjectOutputStream poiStream = null;
		try {
			if (!IOUtils.createParentIfNotExists(poiFile)) {
				Logger.get().log("Can not write pois...");
				return 0;
			}

			int poiSerialized = 0;
			poiStream = new ObjectOutputStream(new FileOutputStream(poiFile, false));
			poiStream.writeInt(pois.size());
			for (POI poi : pois.values()) {
				POISerializer.get().writePOI(poi, poiStream);
				poiSerialized++;
			}
			String poiActiveName = activePOI != null ? activePOI.getName() : "";
			poiStream.writeUTF(poiActiveName);
			Logger.get().log("Pois serailized: " + poiSerialized);
			deleteOldPois();
		} catch (Exception e) {
			Logger.get().log("Poi write error ", e);
		} finally {
			IOUtils.close(poiStream);

		}

		return pois.size();
	}

	private void deleteOldPois() {
		File oldFile = new File(IOUtils.getStorageDirectory(), "pois");
		if (oldFile.exists() && oldFile.canWrite()) {
			oldFile.delete();
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
}
