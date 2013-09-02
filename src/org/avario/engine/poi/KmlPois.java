package org.avario.engine.poi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.avario.utils.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.AsyncTask;

public class KmlPois {
	private AsyncTask<Integer, POI, Integer> thr;
	private double latitude;
	private double longitude;
	private int distance;

	public KmlPois(double latitude, double longitude, int distance) {
		thr = new AsyncTask<Integer, POI, Integer>() {
			@Override
			protected Integer doInBackground(Integer... params) {
				try {
					String kmlStr = kmlString();
					List<POI> pois = getPoisFromKml(kmlStr);
					publishProgress(pois.toArray(new POI[0]));
				} catch (Exception e) {
					Logger.get().log("kml task exception ", e);
				} finally {
					Logger.get().log("kml task terminated");
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(final POI... pois) {
				try {
					onPoi(pois);
				} catch (Exception e) {
					Logger.get().log("poi exception ", e);
				}
			}

		};
		this.latitude = latitude;
		this.longitude = longitude;
		this.distance = distance;
	}

	public void start() {
		thr.execute();
	}

	public String kmlString() throws IOException {
		ZipInputStream zis = null;
		try {
			String preUrlStr = "http://www.paraglidingearth.com/pgearth/kml_files.php?distance=" + distance + "&center=&lat=" + latitude
					+ "&lng=" + longitude;
			URL preUrl = new URL(preUrlStr);
			URLConnection preCon = preUrl.openConnection();
			preCon.connect();
			preUrl.openStream().close();
			// http://www.paraglidingearth.com/pgearth/sites_around.php?lng=25.1&lat=42.33&dist=100
			String urlStr = "http://www.paraglidingearth.com/googleearth/" + distance + "km_around_" + normalize(latitude) + "-"
					+ normalize(longitude) + ".kmz";
			URL url = new URL(urlStr);
			URLConnection ucon = url.openConnection();
			ucon.connect();
			zis = new ZipInputStream(url.openStream());
			zis.getNextEntry();
			java.util.Scanner s = new java.util.Scanner(zis, "UTF-8").useDelimiter("\\A");
			String content = s.hasNext() ? s.next() : null;
			return content;
		} finally {
			if (zis != null) {
				zis.close();
			}
		}
	}

	private String normalize(double value) {
		String dec = new DecimalFormat("0.00").format(value);
		while (dec.endsWith("0")) {
			dec = dec.substring(0, dec.length() - 1);
		}
		return dec;
	}

	public List<POI> getPoisFromKml(String kml) throws Exception {
		final List<POI> pois = new ArrayList<POI>();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		DefaultHandler handler = new DefaultHandler() {
			POI currentPOI;
			boolean isName = false;
			boolean isLatLng = false;

			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

				if ("Placemark".equals(qName)) {
					currentPOI = new POI();
				} else if ("coordinates".equals(qName)) {
					isLatLng = true;
				} else if ("name".equals(qName)) {
					isName = true;
				}
			}

			public void endElement(String uri, String localName, String qName) throws SAXException {
				if ("Placemark".equals(qName) && currentPOI.getName() != null && !currentPOI.getName().equals("")) {
					pois.add(currentPOI);
					currentPOI = null;
				}
			}

			public void characters(char ch[], int start, int length) throws SAXException {
				if (isName && currentPOI != null) {
					currentPOI.setName(new String(ch, start, length));
					isName = false;
				} else if (isLatLng && currentPOI != null) {
					String coordinates = new String(ch, start, length);
					StringTokenizer st = new StringTokenizer(coordinates, ",");
					currentPOI.setLongitude(Double.parseDouble(st.nextToken()));
					currentPOI.setLatitude(Double.parseDouble(st.nextToken()));
					isLatLng = false;
				}
			}
		};

		saxParser.parse(new ByteArrayInputStream(kml.getBytes("UTF-8")), handler);

		return pois;
	}

	public void onPoi(POI... poi) {

	}
}
