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
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File appFolder = new File(Environment.getExternalStorageDirectory(), "AVario");
			return appFolder.mkdirs() || (appFolder.exists() && appFolder.canWrite());
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			File appFolder = new File(Environment.getExternalStorageDirectory(), "AVario");
			return appFolder.exists() && appFolder.canRead();
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
		File fRet = (isExternalStorageWritable()) ? new File(Environment.getExternalStorageDirectory(), "AVario")
				: AVarioActivity.CONTEXT.getFilesDir();
		return fRet;
	}
}
