package request;

import java.io.Serializable;
import lombok.*;
@Getter
@AllArgsConstructor

public class UpdateCoordinatesRequest implements Serializable {
    private double latitude;
    private double longitude;


}

