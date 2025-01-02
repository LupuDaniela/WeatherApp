package service;

import model.Location;
import model.User;
import model.UserRole;

import java.sql.*;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class DatabaseService {
    private Connection connection;

    public DatabaseService() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:weather.db");
            initializeTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL
                )
            """);

            // Create locations table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS locations (
                    id INTEGER PRIMARY KEY,
                    latitude REAL NOT NULL,
                    longitude REAL NOT NULL,
                    name TEXT NOT NULL
                )
            """);
        }
    }

    public Optional<User> findUser(String username, String passwordHash) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM users WHERE username = ? AND password_hash = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new User(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        UserRole.valueOf(rs.getString("role"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Location> findNearbyLocations(double lat, double lon, double radiusKm) {
        List<Location> locations = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM locations")) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Location loc = new Location(
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("name")
                );

                if (calculateDistance(lat, lon, loc.getLatitude(), loc.getLongitude()) <= radiusKm) {
                    locations.add(loc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locations;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula implementation
        double R = 6371; // Earth's radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
