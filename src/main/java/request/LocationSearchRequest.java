package request;

import java.io.*;
import lombok.*;
@Getter

public class LocationSearchRequest implements Serializable {
    private double latitude;
    private double longitude;
    private double radius;
    public LocationSearchRequest(double latitude, double longitude, double radius) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be greater than zero.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

}
