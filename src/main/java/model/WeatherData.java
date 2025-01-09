
package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class WeatherData implements Serializable {
    private String condition;
    private double temperature;
    private LocalDateTime timestamp;
    private Location location;
    private List<DailyForecast> forecast;

    public WeatherData(String condition, double temperature, LocalDateTime timestamp, Location location, List<DailyForecast> forecast) {
        this.condition = condition;
        this.temperature = temperature;
        this.timestamp = timestamp;
        this.location = location;
        this.forecast = forecast;
    }

    public String getCondition() {
        return condition;
    }

    public double getTemperature() {
        return temperature;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Location getLocation() {
        return location;
    }

    public List<DailyForecast> getForecast() {
        return forecast;
    }

    @Override
    public String toString() {
        StringBuilder forecastStr = new StringBuilder();
        if (forecast != null) {
            for (DailyForecast day : forecast) {
                forecastStr.append(day.toString()).append("\n");
            }
        }
        return String.format("Location: %s (%.2f, %.2f), Temperature: %.2f°C, Condition: %s\nForecast:\n%s",
                location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                temperature,
                condition,
                forecastStr.toString());
    }
}
