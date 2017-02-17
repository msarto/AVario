package org.avario.utils.filters.impl;

import java.util.ArrayDeque;
import java.util.Queue;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.utils.Logger;
import org.avario.utils.StringFormatter;
import org.avario.utils.filters.Filter;

import android.os.SystemClock;

public class Kalman2Filter implements Filter {

    static class Sample {
        public long timestamp;
        public float value;

        public Sample(float value) {
            this.value = value;
            this.timestamp = SystemClock.elapsedRealtime();
        }

        public String toString() {
            return "value: x " + StringFormatter.multipleDecimals(value);
        }
    }

    protected Queue<Sample> history = new ArrayDeque<Sample>();

    @Override
    public synchronized float[] doFilter(final float... value) {
        history.add(new Sample(value[0]));
        float[] result = new float[]{doKalman2()};
        trimSamples();
        return result;
    }

    private void trimSamples() {
        float sensitivity = DataAccessObject.get().getSensitivity();
        boolean canSample = true;
        while (canSample) {
            // while (history.size() > Math.max(3, medianInterval)) {
            Sample item = history.peek();
            if (SystemClock.elapsedRealtime() - item.timestamp > sensitivity*50)
                history.poll();
            else {
                canSample = false;
            }
            //Logger.get().log("Hist size: " + history.size() + " s: " + sensitivity);
            sensitivity = DataAccessObject.get().getSensitivity();
        }
    }

    protected float doKalman2() {
        float median = 0;
        int N = history.size();
        if (N == 1) {
            return history.peek().value;
        }
        float I = 0;
        for (Sample item : history) {
            median += item.value * I;
            I = I + 1f / (N * (N - 1));
        }
        return median * 2;
    }

    public synchronized void reset() {
        history.clear();
    }

    public static void main(String[] args) {
        Kalman2Filter filter = new Kalman2Filter();
        filter.doFilter(1);
        filter.doFilter(3);
        filter.doFilter(5);
        filter.doFilter(1);
        filter.doFilter(1);
        filter.doFilter(1);
    }

}