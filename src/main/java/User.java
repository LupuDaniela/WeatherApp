import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class User {
    private String username;
    private String password;
    private UserRole role;
    public User(String username, String password,UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
