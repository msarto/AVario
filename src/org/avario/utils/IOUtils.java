package org.avario.utils;

import java.io.Closeable;
import java.io.File;

import org.avario.AVarioActivity;

import android.os.Environment;

public class IOUtils {

	public static boolean createParentIfNotExists(File f) {
		if (f.exists()) {
			return f.canWrite();
		} else {
			return (f.getParentFile().exists()) ? true : f.getParentFile().mkdir();
		}
	}

	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		boolean bRights = false;
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			bRights = Environment.getExternalStorageDirectory().canWrite();
			if (bRights) {
				File appFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "AVario");
				bRights = appFolder.mkdirs() || appFolder.exists();
			}
		}
		return bRights;
	}

	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return Environment.getExternalStorageDirectory().canRead();
		}
		return false;
	}

	public static void close(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (Exception e) {
				Logger.get().log("Fail closing io ", e);
			}
		}
	}

	public static File getStorageDirectory() {
		return (isExternalStorageWritable()) ? new File(Environment.getExternalStorageDirectory() + File.separator
				+ "AVario") : AVarioActivity.CONTEXT.getFilesDir();
	}

}
