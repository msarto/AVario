package org.avario.utils.filters.impl;

import org.avario.utils.filters.Filter;

/**
 * Created by mihai on 10/22/17.
 */

public class BufferFilter implements Filter {

    private float previousValue[] = null;
    private float maxDiff = 0;

    public BufferFilter(float maxDiff) {
        this.maxDiff = maxDiff;
    }

    @Override
    public float[] doFilter(float... value) {
        float toFilter = value[0];
        float ret[];
        if (previousValue != null) {
            float diff = toFilter - previousValue[0];
            ret = (Math.abs(diff) > maxDiff) ? new float[]{previousValue[0] + diff * 0.3f} : value.clone();
        } else {
            ret = value.clone();
        }
        previousValue = ret;
        return ret;
    }

    @Override
    public void reset() {
        previousValue = null;
    }
}
