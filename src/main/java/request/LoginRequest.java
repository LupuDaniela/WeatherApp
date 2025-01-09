package request;

import java.io.*;
import lombok.*;
@Getter


public class LoginRequest implements Serializable{
    private String username;
    private String password;
    public LoginRequest(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        this.username = username;
        this.password = password;
    }
    @Override
    public String toString() {
        return "LoginRequest{username='" + username + "'}";
    }


}
