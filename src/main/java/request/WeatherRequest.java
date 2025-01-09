package request;

import model.Location;

import java.io.Serializable;

public class WeatherRequest implements Serializable {
    private Location location;

    public WeatherRequest(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}

