package model;

import java.io.Serializable;

public class DailyForecast implements Serializable {
    private final double temperature;
    private String date;
    private String condition;

    public DailyForecast(String date, double temperature, String condition) {
        this.date = date;
        this.temperature = temperature;
        this.condition = condition;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return String.format("Date: %s, Temp: %.2f°C, Condition: %s", date, temperature, condition);
    }
}
