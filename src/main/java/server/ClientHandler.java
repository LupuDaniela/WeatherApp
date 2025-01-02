package server;
import request.*;
import response.*;
import model.*;
import service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private DatabaseService databaseService;
    private WeatherService weatherService;
    private User currentUser;

    public ClientHandler(Socket socket, DatabaseService databaseService) {
        this.clientSocket = socket;
        this.databaseService = databaseService;
        this.weatherService = new WeatherService();

        try {
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object request = input.readObject();
                handleRequest(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Object request) throws IOException {
        if (request instanceof LoginRequest) {
            handleLogin((LoginRequest) request);
        } else if (request instanceof WeatherRequest) {
            handleWeatherRequest((WeatherRequest) request);
        } else if (request instanceof LocationSearchRequest) {
            handleLocationSearch((LocationSearchRequest) request);
        } else if (request instanceof JsonImportRequest && isAdmin()) {
            handleJsonImport((JsonImportRequest) request);
        }
    }

    private boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }
    private void handleLogin(LoginRequest request) throws IOException {
        try {
            String passwordHash = hashPassword(request.getPassword());
            Optional<User> user = databaseService.findUser(request.getUsername(), passwordHash);

            if (user.isPresent()) {
                currentUser = user.get();
                output.writeObject(new Response(true, "Login successful"));
            } else {
                output.writeObject(new Response(false, "Invalid credentials"));
            }
        } catch (Exception e) {
            output.writeObject(new Response(false, "Login failed: " + e.getMessage()));
        }
    }

    private void handleWeatherRequest(WeatherRequest request) throws IOException {
        try {
            if (currentUser == null) {
                output.writeObject(new Response(false, "Please login first"));
                return;
            }

            WeatherData weatherData = weatherService.getWeatherData(request.getLocation());
            output.writeObject(new Response(true, weatherData));
        } catch (Exception e) {
            output.writeObject(new Response(false, "Weather request failed: " + e.getMessage()));
        }
    }

    private void handleLocationSearch(LocationSearchRequest request) throws IOException {
        try {
            if (currentUser == null) {
                output.writeObject(new Response(false, "Please login first"));
                return;
            }

            List<Location> nearbyLocations = databaseService.findNearbyLocations(
                    request.getLatitude(),
                    request.getLongitude(),
                    request.getRadius()
            );

            output.writeObject(new Response(true, nearbyLocations));
        } catch (Exception e) {
            output.writeObject(new Response(false, "Location search failed: " + e.getMessage()));
        }
    }

    private void handleJsonImport(JsonImportRequest request) throws IOException {
        try {
            if (!isAdmin()) {
                output.writeObject(new Response(false, "Admin privileges required"));
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            List<WeatherData> weatherDataList = mapper.readValue(
                    request.getJsonData(),
                    new TypeReference<List<WeatherData>>() {}
            );

            // Process and store the weather data
            // This is a simplified example - you might want to add more validation and error handling
            output.writeObject(new Response(true, "JSON data imported successfully"));
        } catch (Exception e) {
            output.writeObject(new Response(false, "JSON import failed: " + e.getMessage()));
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}