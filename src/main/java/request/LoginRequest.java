package request;

import java.io.*;
import lombok.*;
@Getter
@AllArgsConstructor

public class LoginRequest implements Serializable{
    private String username;
    private String password;
}
