//
//package model;
//
//import java.io.Serializable;
//import java.time.LocalDateTime;
//import lombok.*;
//
//@Getter
//@Setter
//@AllArgsConstructor
//public class WeatherData implements Serializable {
//
//
//    private String condition;
//    private double temperature;
//    private LocalDateTime timestamp;
//    private Location location;
//
//
//    public WeatherData() {}
//
//
//
////    public WeatherData(double temperature, double humidity, String condition,
////                       String windDirection, double windSpeed, LocalDateTime timestamp) {
////        setTemperature(temperature);
////        setHumidity(humidity);
////        this.condition = condition;
////        this.windDirection = windDirection;
////        setWindSpeed(windSpeed);
////        this.timestamp = timestamp;
////    }
//
//    public void setTemperature(double temperature) {
//        if (temperature < -100 || temperature > 100) {
//            throw new IllegalArgumentException("Temperature must be realistic.");
//        }
//        this.temperature = temperature;
//    }
//
//
//
//    @Override
//    public String toString() {
//        return String.format("Location: %s (%.2f, %.2f), Temperature: %.2f°C, Condition: %s",
//                location.getName(),
//                location.getLatitude(),  // Ensure this is double
//                location.getLongitude(), // Ensure this is double
//                temperature,             // Ensure this is double
//                condition);              // Ensure this is a String
//    }
//
//
//
//
//}
//
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
