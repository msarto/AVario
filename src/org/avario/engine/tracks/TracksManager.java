package org.avario.engine.tracks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.avario.AVarioActivity;
import org.avario.utils.IOUtils;
import org.avario.utils.Logger;

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
		try {
			File externalTracks = IOUtils.getExternalStorageDirectory();
			if (externalTracks != null) {
				File rootFolder = new File(externalTracks, "AVario");
				readTracks(rootFolder);
			}
			readTracks(AVarioActivity.CONTEXT.getFilesDir());

			Collections.sort(tracks, new Comparator<TrackInfo>() {
				@Override
				public int compare(TrackInfo lhs, TrackInfo rhs) {
					return lhs.getFlightStart() > rhs.getFlightStart() ? -1 : 1;
				}
			});
		} catch (Throwable e) {
			Logger.get().log("Fail reading tracks ", e);
		}
		return tracks.size();
	}

	private void readTracks(File rootFolder) throws FileNotFoundException, IOException {
		if (rootFolder != null && rootFolder.exists() && rootFolder.canRead() && rootFolder.isDirectory()) {
			File metaFiles[] = rootFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".meta");
				}
			});

			Logger.get().log("Found " + metaFiles != null ? "" + metaFiles.length : "null" + " tracks");
			for (File metaFile : metaFiles) {
				TrackInfo trackInfo = new TrackInfo();
				trackInfo.readFrom(metaFile);
				tracks.add(trackInfo);
			}
		}
	}
}
