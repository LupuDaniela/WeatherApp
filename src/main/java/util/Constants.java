//package util;
//
//public final class Constants{
//    public static final String API_KEY="6e05c92312264726834130854242812";
//    public static final String API_BASE_URL = "http://api.weatherapi.com/v1/current.json";
//    public static final int SERVER_PORT = 8080;
//    public static final String DB_URL = "jdbc:postgresql://localhost:5432/weatherapp";
//    public static final String DB_USER = "postgres";
//    public static final String DB_PASSWORD = "1q2w3e";
//}

package util;

public final class Constants {
    public static final String API_KEY = "WeatherAPI";
    public static final String API_BASE_URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m";
    public static final int SERVER_PORT = 8080;
    public static final String DB_URL = "jdbc:postgresql://localhost:5432/weatherapp";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "1q2w3e";


    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated.");
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            System.out.println("Environment variable " + key + " is not set. Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }
}
