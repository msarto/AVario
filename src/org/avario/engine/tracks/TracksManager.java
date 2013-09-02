package org.avario.engine.tracks;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.avario.utils.Logger;

import android.os.Environment;

public class TracksManager {
	private static TracksManager THIS = new TracksManager();
	private final List<TrackInfo> tracks = new ArrayList<TrackInfo>();

	protected TracksManager() {
	}

	public static TracksManager get() {
		return THIS;
	}

	public List<TrackInfo> getTracks() {
		return tracks;
	}

	public int readTracks() {
		tracks.clear();
		File rootFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "AVario");
		if (rootFolder.exists()) {
			File metaFiles[] = rootFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".meta");
				}
			});

			Logger.get().log("Found " + metaFiles != null ? "" + metaFiles.length : "null" + " tracks");
			for (File metaFile : metaFiles) {
				TrackInfo trackInfo = new TrackInfo();
				try {
					trackInfo.readFrom(metaFile);
					tracks.add(trackInfo);
				} catch (Exception e) {
					Logger.get().log("Fail reading file " + metaFile.getName(), e);
				}
			}
		}
		Collections.sort(tracks, new Comparator<TrackInfo>() {
			@Override
			public int compare(TrackInfo lhs, TrackInfo rhs) {
				return lhs.getFlightStart() > rhs.getFlightStart() ? -1 : 1;
			}
		});
		return tracks.size();
	}
}
