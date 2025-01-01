import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WeatherService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public WeatherData getWeatherData(Location location) throws Exception{
        String url=String.format("%s?key=%s&q=%f,%f",
                Constants.API_BASE_URL,
                Constants.API_KEY,
                location.getLatitude(),
                location.getLongitude()
        );
        HttpRequest request= HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String>response=httpClient.send(request, HttpResponse.BodyHandlers.ofString());
         return objectMapper.readValue(response.body(),WeatherData.class);
    }
}
