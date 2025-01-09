
package service;

import model.*;
import util.Constants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

public class DatabaseService {
    private static final Logger logger = Logger.getLogger(DatabaseService.class.getName());
    private Connection connection;

    public DatabaseService() {
        try {
            connection = DriverManager.getConnection(Constants.DB_URL, Constants.DB_USER, Constants.DB_PASSWORD);

            initializeTables();
        } catch (SQLException e) {
            logger.severe("Error connecting to database: " + e.getMessage());
        }
    }

    private void initializeTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "username VARCHAR(50) PRIMARY KEY, "
                    + "password_hash VARCHAR(64) NOT NULL, "
                    + "role VARCHAR(10) NOT NULL"
                    + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS locations ("
                    + "id SERIAL PRIMARY KEY, "
                    + "latitude DOUBLE PRECISION NOT NULL, "
                    + "longitude DOUBLE PRECISION NOT NULL, "
                    + "name VARCHAR(100) NOT NULL"
                    + ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS weather_data ("
                    + "id SERIAL PRIMARY KEY, "
                    + "latitude DOUBLE PRECISION NOT NULL, "
                    + "longitude DOUBLE PRECISION NOT NULL, "
                    + "temperature DOUBLE PRECISION NOT NULL, "
                    + "humidity DOUBLE PRECISION, "
                    + "condition VARCHAR(50), "
                    + "wind_direction VARCHAR(50), "
                    + "wind_speed DOUBLE PRECISION, "
                    + "timestamp TIMESTAMP NOT NULL"
                    + ")");

        }
    }

    public Optional<User> findUser(String username, String passwordHash) {
        System.out.println("Finding user with username: " + username);
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT username, role FROM users WHERE username = ? AND password_hash = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("User found with username: " + username + " and role: " + rs.getString("role"));
                return Optional.of(new User(
                        rs.getString("username"),
                        null,
                        UserRole.valueOf(rs.getString("role"))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("User not found with username: " + username);
        return Optional.empty();
    }




    public List<Location> findNearbyLocations(double lat, double lon, double radiusKm) {
        List<Location> locations = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM locations")) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Location loc = new Location(
                        rs.getString("name"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                );

                if (calculateDistance(lat, lon, loc.getLatitude(), loc.getLongitude()) <= radiusKm) {
                    locations.add(loc);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding locations: " + e.getMessage());
        }
        return locations;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.severe("Error closing database connection: " + e.getMessage());
        }
    }
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }
    public void saveWeatherData(Location location, WeatherData weatherData) {
        String sql = "INSERT INTO weather_data (latitude, longitude, temperature, humidity, condition, wind_direction, wind_speed, timestamp) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";


        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, location.getLatitude());
            stmt.setDouble(2, location.getLongitude());
            stmt.setDouble(3, weatherData.getTemperature());
            stmt.setString(4, weatherData.getCondition());
            stmt.setTimestamp(5, Timestamp.valueOf(weatherData.getTimestamp()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<WeatherData> findWeatherData(Location location) {
        String query = "SELECT * FROM weather_data WHERE latitude = ? AND longitude = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, location.getLatitude());
            stmt.setDouble(2, location.getLongitude());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Location locationObj = new Location(
                        rs.getString("name"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                );


                List<DailyForecast> forecast = getForecastFromDatabase(locationObj);

                return Optional.of(new WeatherData(
                        rs.getString("condition"),
                        rs.getDouble("temperature"),
                        rs.getTimestamp("timestamp").toLocalDateTime(),
                        locationObj,
                        forecast
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private List<DailyForecast> getForecastFromDatabase(Location location) {
        List<DailyForecast> forecast = new ArrayList<>();
        String query = "SELECT * FROM forecast_data WHERE latitude = ? AND longitude = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, location.getLatitude());
            stmt.setDouble(2, location.getLongitude());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                forecast.add(new DailyForecast(
                        rs.getString("date"),
                        rs.getDouble("temperature"),
                        rs.getString("condition")
                ));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return forecast;
    }



    public boolean isUsernameTaken(String username) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT 1 FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void createUser(String username, String passwordHash, String role) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, role);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateCoordinates(double latitude, double longitude) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE locations SET latitude = ?, longitude = ? WHERE id = 1")) {
            stmt.setDouble(1, latitude);
            stmt.setDouble(2, longitude);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




}

