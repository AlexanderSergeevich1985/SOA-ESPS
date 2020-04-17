package com.soaesps.core.DataModels.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseOnlyIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "GPS_POSITION")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GPSPosition extends BaseOnlyIdEntity implements Serializable {
    @Column(name = "longitude", columnDefinition = "FLOAT(126)")
    private Double longitude;

    @Column(name = "latitude", columnDefinition = "FLOAT(126)")
    private Double latitude;

    @Column(name = "altitude", columnDefinition = "FLOAT(126)")
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
}