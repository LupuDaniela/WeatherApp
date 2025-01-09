package request;

import java.io.Serializable;
import lombok.*;
@Getter
@AllArgsConstructor
public class RegisterRequest implements Serializable {
    private String username;
    private String password;
    private String role;

}
