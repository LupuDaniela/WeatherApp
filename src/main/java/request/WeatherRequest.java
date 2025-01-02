package request;

import java.io.*;
import lombok.*;
import model.Location;

@Getter
@AllArgsConstructor

public class WeatherRequest implements Serializable{
    private Location location;
}
