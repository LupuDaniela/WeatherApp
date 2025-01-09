
package service;

import model.DailyForecast;
import model.Location;
import model.WeatherData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {
    private static final String API_KEY = "e71b9eec80ac4229baeb0f01d5d83674";
    private static final String WEATHER_URL = "https://api.weatherbit.io/v2.0/forecast/daily";


public WeatherData getWeatherData(Location location) throws Exception {
    String url;
    if (location.getName() != null && !location.getName().isEmpty()) {
        // Search by location name
        url = String.format("%s?city=%s&key=%s", WEATHER_URL, location.getName(), API_KEY);
    } else {
        // Search by coordinates
        url = String.format("%s?lat=%.2f&lon=%.2f&key=%s", WEATHER_URL,
                location.getLatitude(), location.getLongitude(), API_KEY);
    }

    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestMethod("GET");

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    StringBuilder response = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
        response.append(line);
    }
    reader.close();

    JSONObject jsonResponse = new JSONObject(response.toString());
    JSONArray dailyData = jsonResponse.getJSONArray("data");


    JSONObject currentDay = dailyData.getJSONObject(0);
    JSONObject weatherObject = currentDay.getJSONObject("weather");
    String condition = weatherObject.getString("description");
    double temperature = currentDay.getDouble("temp");


    List<DailyForecast> forecast = new ArrayList<>();
    for (int i = 1; i < dailyData.length(); i++) {
        JSONObject day = dailyData.getJSONObject(i);
        String date = day.getString("valid_date");
        String dayCondition = day.getJSONObject("weather").getString("description");
        double temp = day.getDouble("temp"); // Single temperature

        forecast.add(new DailyForecast(date, temp, dayCondition));
    }

    return new WeatherData(condition, temperature, LocalDateTime.now(), location, forecast);
}


}
