package org.avario.engine.tracks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.avario.AVarioActivity;
import org.avario.utils.Logger;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class TrackInfo {
	private long flightLenght = 0;
	private long flightStart = 0;
	private long flightDuration = 0;
	private int startAlt = 0;
	private int endAlt = 0;
	private int highestAlt = 0;
	private float highestVSpeed = 0f;
	private float maxSpeed = 0f;
	private float highestSink = 0f;
	private String trackFileName;
	private long versionCode = 0;

	public String getTrackFileName() {
		return trackFileName;
	}

	public long getFlightLenght() {
		return flightLenght;
	}

	public void setFlightLenght(long flightLenght) {
		this.flightLenght = flightLenght;
	}

	public long getFlightStart() {
		return flightStart;
	}

	public void setFlightStart(long flightStart) {
		this.flightStart = flightStart;
	}

	public long getFlightDuration() {
		return flightDuration;
	}

	public long getVersionCode() {
		return versionCode;
	}

	public void setFlightDuration(long flightDuration) {
		this.flightDuration = flightDuration;
	}

	public int getStartAlt() {
		return startAlt;
	}

	public void setStartAlt(int startAlt) {
		this.startAlt = startAlt;
	}

	public int getEndAlt() {
		return endAlt;
	}

	public void setEndAlt(int endAlt) {
		this.endAlt = endAlt;
	}

	public int getHighestAlt() {
		return highestAlt;
	}

	public void setHighestAlt(int highestAlt) {
		this.highestAlt = highestAlt;
	}

	public float getHighestVSpeed() {
		return highestVSpeed;
	}

	public void setHighestVSpeed(float highestVSpeed) {
		this.highestVSpeed = highestVSpeed;
	}

	public float getHighestSink() {
		return highestSink;
	}

	public void setHighestSink(float highestSink) {
		this.highestSink = highestSink;
	}

	public float getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public void readFrom(File file) throws FileNotFoundException, IOException {
		ObjectInputStream trackStream = new ObjectInputStream(new FileInputStream(file));
		long stItem = trackStream.readLong();
		if (stItem > 1000000l && stItem < 1001000l) {
			versionCode = stItem - 1000000l;
			Logger.get().log("Reading track item from version " + versionCode);
			stItem = trackStream.readLong();
		}
		flightLenght = stItem;
		flightStart = trackStream.readLong();
		flightDuration = trackStream.readLong();
		startAlt = trackStream.readInt();
		endAlt = trackStream.readInt();
		highestAlt = trackStream.readInt();
		highestVSpeed = trackStream.readFloat();
		highestSink = trackStream.readFloat();
		maxSpeed = trackStream.readFloat();
		trackFileName = file.getAbsolutePath();
		trackStream.close();
	}

	public void writeTo(File file) throws FileNotFoundException, IOException {
		ObjectOutputStream trackStream = new ObjectOutputStream(new FileOutputStream(file));
		Logger.get().log("Start Serializing Track meta ");
		try {
			PackageInfo pInfo = AVarioActivity.CONTEXT.getPackageManager().getPackageInfo(
					AVarioActivity.CONTEXT.getPackageName(), 0);
			trackStream.writeLong(1000000l + pInfo.versionCode);
		} catch (NameNotFoundException e) {
			Logger.get().log("Fail seraializing track app version", e);
		}
		trackStream.writeLong(flightLenght);
		trackStream.writeLong(flightStart);
		trackStream.writeLong(flightDuration);
		trackStream.writeInt(startAlt);
		trackStream.writeInt(endAlt);
		trackStream.writeInt(highestAlt);
		trackStream.writeFloat(highestVSpeed);
		trackStream.writeFloat(highestSink);
		trackStream.writeFloat(maxSpeed);
		trackStream.close();

		StringBuilder sb = new StringBuilder();
		sb.append(flightLenght).append("; ");
		sb.append(flightStart).append("; ");
		sb.append(flightDuration).append("; ");
		sb.append(startAlt).append("; ");
		sb.append(endAlt).append("; ");
		sb.append(highestAlt).append("; ");
		sb.append(highestVSpeed).append("; ");
		sb.append(highestSink).append("; ");
		Logger.get().log("Serialized Track meta: " + sb.toString());
	}
}
