package com.soaesps.core.DataModels.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseOnlyIdEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "GPS_POSITION")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GPSPosition extends BaseOnlyIdEntity implements Serializable {
    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "altitude")
    private Double altitude;

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(final Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(final Double latitude) {
        this.latitude = latitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(final Double altitude) {
        this.altitude = altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GPSPosition that = (GPSPosition) o;
        return Objects.equals(longitude, that.longitude) &&
                Objects.equals(latitude, that.latitude) &&
                Objects.equals(altitude, that.altitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), longitude, latitude, altitude);
    }
}