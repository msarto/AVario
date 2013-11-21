package org.avario.utils.filters.impl;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.utils.filters.Filter;

import android.os.SystemClock;

public class MedianFilter implements Filter {
	private long historyTime;
	protected Queue<AltitudeStamp> history = new ArrayDeque<AltitudeStamp>();

	protected static class AltitudeStamp {
		long time = 0;
		float baro = 0.0f;

		AltitudeStamp(long time, float baro) {
			this.time = time;
			this.baro = baro;
		}
	}

	public MedianFilter(long historyTime) {
		this.historyTime = historyTime;
	}

	@Override
	public synchronized float[] doFilter(float... value) {
		float[] result = new float[1];
		history.add(new AltitudeStamp(SystemClock.elapsedRealtime(), value[0]));
		result[0] = medianDoMedian();
		AltitudeStamp stamp = history.peek();
		if (stamp.time + historyTime < SystemClock.elapsedRealtime()) {
			history.poll();
		}
		return result;
	}

	protected float medianDoMedian() {
		float median = 0.0f;
		for (AltitudeStamp item : history) {
			median += item.baro;
		}
		return median / (float) history.size();
	}

	public synchronized void reset() {
		historyTime = 0;
		history.clear();
	}

}
