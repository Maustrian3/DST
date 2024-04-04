package dst.ass1.jooq.model.impl;


import dst.ass1.jooq.model.IRiderPreference;

import java.util.Map;
import java.util.Objects;

public class RiderPreference implements IRiderPreference {
    private Long riderId;
    private String vehicleClass;
    private String area;
    private Map<String, String> preferences;

    public RiderPreference() {
    }

    @Override
    public Long getRiderId() {
        return riderId;
    }

    @Override
    public void setRiderId(Long riderId) {
        this.riderId = riderId;
    }

    @Override
    public String getVehicleClass() {
        return vehicleClass;
    }

    @Override
    public void setVehicleClass(String vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    @Override
    public String getArea() {
        return area;
    }

    @Override
    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public Map<String, String> getPreferences() {
        return preferences;
    }

    @Override
    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RiderPreference that = (RiderPreference) o;
        return Objects.equals(riderId, that.riderId)
                && Objects.equals(vehicleClass, that.vehicleClass)
                && Objects.equals(area, that.area)
                && Objects.equals(preferences, that.preferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(riderId, vehicleClass, area, preferences);
    }
}
