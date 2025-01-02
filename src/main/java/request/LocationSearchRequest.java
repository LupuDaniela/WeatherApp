package request;

import java.io.*;
import lombok.*;
@Getter
@AllArgsConstructor
public class LocationSearchRequest implements Serializable {
    private double latitude;
    private double longitude;
    private double radius;
}
