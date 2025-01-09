
package model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String password;
    private UserRole role;

    public void setPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        this.password = hashPassword(rawPassword);
    }

    private String hashPassword(String password) {

        return Integer.toHexString(password.hashCode());
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        this.username = username;
    }
}

