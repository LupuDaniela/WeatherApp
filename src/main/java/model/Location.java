package model;

import java.util.Objects;
import lombok.*;
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String name;
    private double latitude;
    private double longitude;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.latitude,latitude)==0 &&
                Double.compare(location.longitude,longitude) == 0&&
                Objects.equals(name,location.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, latitude, longitude);
    }
}
