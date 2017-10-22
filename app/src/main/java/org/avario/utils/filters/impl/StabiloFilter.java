package org.avario.utils.filters.impl;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;

public class StabiloFilter extends IIRFilter {

    public StabiloFilter() {
        super(DataAccessObject.get().getSensitivity() * 0.01);
    }

    @Override
    public synchronized float[] doFilter(final float... nowValues) {

        filterFactor = DataAccessObject.get().getSensitivity() * 0.01;
        float currentValue = nowValues[0];
//        if (previousValues == null) {
//            previousValues = nowValues;
//        }
//        float maxDiff = DataAccessObject.get().getSensitivity() / 600f;
//        // Accept a maximum difference of ~ 0.02
//        float diff = currentValue - previousValues[0];
//        diff = Math.min(maxDiff, Math.abs(diff)) * Math.signum(diff);
        return super.doFilter(new float[]{currentValue});
    }
}
