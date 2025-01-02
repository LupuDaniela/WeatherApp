package response;

import java.io.Serializable;
import lombok.*;
@Getter
@AllArgsConstructor
public class Response implements Serializable {
    private boolean success;
    private Object data;
}