package org.avario.utils.filters.sensors;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.filters.Filter;
import android.hardware.SensorManager;

public class BaroSensorFilter {

    private Filter[] baroFilters;
    private volatile float referrencePresure = Preferences.ref_qnh;

    public BaroSensorFilter(Filter... filters) {
        //SensorProducer.get().registerConsumer(this);
        baroFilters = filters;
    }

    // filter the pressure and transform it to altitude
    public synchronized float toAltitude(float currentPresure) {
        float lastPresureNotified = currentPresure;
        for (Filter filter : baroFilters) {
            lastPresureNotified = filter.doFilter(lastPresureNotified)[0];
        }

        DataAccessObject.get().setLastPresure(lastPresureNotified);
        if (referrencePresure != Preferences.ref_qnh) {
            resetFilters();
            // altitudeFilter.reset();
            DataAccessObject.get().resetVSpeed();
            referrencePresure = Preferences.ref_qnh;
        }
        float sensorAlt = SensorManager.getAltitude(referrencePresure, lastPresureNotified);
        return sensorAlt;
    }

    private void resetFilters() {
        if (baroFilters != null) {
            for (Filter filter : baroFilters) {
                filter.reset();
            }
        }
    }
}
