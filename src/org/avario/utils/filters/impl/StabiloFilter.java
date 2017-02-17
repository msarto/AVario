package org.avario.utils.filters.impl;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;

public class StabiloFilter extends IIRFilter {

    public StabiloFilter() {
        super(0.7f - DataAccessObject.get().getSensitivity() / 100f);
    }

    @Override
    public synchronized float[] doFilter(final float... nowValues) {

        filterFactor = 0.7f - DataAccessObject.get().getSensitivity() / 100f;
        float currentValue = nowValues[0];
        if (previousValues == null) {
            previousValues = nowValues;
        }
        float maxDiff = DataAccessObject.get().getSensitivity() / 600f;
        // Accept a maximum difference of ~ 0.02
        float diff = currentValue - previousValues[0];
        diff = Math.min(maxDiff, Math.abs(diff)) * Math.signum(diff);
        return super.doFilter(new float[]{previousValues[0] + diff});
    }
}
