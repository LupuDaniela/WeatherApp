package model;

import java.time.LocalDateTime;
import lombok.*;
@Getter
@Setter

public class WeatherData {
    private double temperature;
    private double humidity;
    private String condition;
    private String windDirection;
    private double windSpeed;
    private LocalDateTime timestamp;

    // Default constructor for JSON deserialization
    public WeatherData() {}

    public WeatherData(double temperature, double humidity, String condition,
                       String windDirection, double windSpeed, LocalDateTime timestamp) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.condition = condition;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.timestamp = timestamp;
    }
}
