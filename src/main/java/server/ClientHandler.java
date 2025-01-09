
package server;

import request.*;
import response.*;
import model.*;
import service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
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
            logger.severe("Error initializing client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (!clientSocket.isClosed()) {
                Object request = input.readObject();
                handleRequest(request);
            }
        } catch (IOException e) {
            logger.warning("Client disconnected: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Error handling client request: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void handleRequest(Object request) throws IOException {
        if (request instanceof LoginRequest) {
            handleLogin((LoginRequest) request);
        } else if (request instanceof RegisterRequest) {
            handleRegister((RegisterRequest) request);
        } else if (request instanceof WeatherRequest) {
            handleWeatherRequest((WeatherRequest) request);
        } else if (request instanceof UpdateCoordinatesRequest) {
            if (isAdmin()) {
                handleUpdateCoordinates((UpdateCoordinatesRequest) request);
            } else {
                output.writeObject(new Response<>(false, null, "Unauthorized: Admin privileges required"));
            }
        } else {
            output.writeObject(new Response<>(false, null, "Unknown or unauthorized request"));
        }
    }

    private boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }

    private void handleLogin(LoginRequest request) throws IOException {
        try {
            String passwordHash = databaseService.hashPassword(request.getPassword());
            Optional<User> user = databaseService.findUser(request.getUsername(), passwordHash);

            if (user.isPresent()) {
                currentUser = user.get();
                output.writeObject(new Response<>(true, currentUser.getRole().name(), "Login successful"));
            } else {
                output.writeObject(new Response<>(false, null, "Invalid credentials"));
            }
        } catch (Exception e) {
            output.writeObject(new Response<>(false, null, "Login failed: " + e.getMessage()));
        }
    }

    private void handleRegister(RegisterRequest request) throws IOException {
        try {
            if (databaseService.isUsernameTaken(request.getUsername())) {
                output.writeObject(new Response<>(false, null, "Username already exists"));
                return;
            }

            String passwordHash = databaseService.hashPassword(request.getPassword());
            databaseService.createUser(request.getUsername(), passwordHash, request.getRole());
            output.writeObject(new Response<>(true, null, "Registration successful"));
        } catch (Exception e) {
            output.writeObject(new Response<>(false, null, "Registration failed: " + e.getMessage()));
        }
    }



    private void handleWeatherRequest(WeatherRequest request) throws IOException {
        try {
            if (currentUser == null) {
                output.writeObject(new Response<>(false, null, "Please login first"));
                return;
            }

            Location location = request.getLocation();
            Optional<WeatherData> cachedData = databaseService.findWeatherData(location);

            WeatherData weatherData;
            if (cachedData.isPresent() && isDataFresh(cachedData.get())) {
                weatherData = cachedData.get();
            } else {
                weatherData = weatherService.getWeatherData(location);
                databaseService.saveWeatherData(location, weatherData);
            }

            output.writeObject(new Response<>(true, weatherData, "Weather data retrieved successfully"));
        } catch (Exception e) {
            output.writeObject(new Response<>(false, null, "Weather request failed: " + e.getMessage()));
        }
    }

    private boolean isDataFresh(WeatherData weatherData) {
        return weatherData.getTimestamp().isAfter(LocalDateTime.now().minusHours(1));
    }

    private void handleUpdateCoordinates(UpdateCoordinatesRequest request) throws IOException {
        try {
            databaseService.updateCoordinates(request.getLatitude(), request.getLongitude());
            output.writeObject(new Response<>(true, null, "Coordinates updated successfully"));
        } catch (Exception e) {
            output.writeObject(new Response<>(false, null, "Failed to update coordinates: " + e.getMessage()));
        }
    }

    private void close() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (input != null) input.close();
            if (output != null) output.close();
        } catch (IOException e) {
            logger.severe("Error closing client handler: " + e.getMessage());
        }
    }
}

