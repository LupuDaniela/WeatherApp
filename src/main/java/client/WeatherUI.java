package client;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import request.LoginRequest;
import request.WeatherRequest;
import request.LocationSearchRequest;
import request.JsonImportRequest;
import model.Location;
import response.Response;

import java.io.File;
import java.nio.file.Files;

public class WeatherUI {
    private WeatherClient client;
    private Stage primaryStage;
    private VBox mainLayout;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField latitudeField;
    private TextField longitudeField;
    private TextField radiusField;
    private TextArea resultArea;
    private Button loginButton;
    private Button searchButton;
    private Button importButton;

    public WeatherUI(WeatherClient client) {
        this.client = client;
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        createLoginForm();
        createWeatherSearch();
        createAdminPanel();

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setTitle("Weather Information System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createLoginForm() {
        GridPane loginGrid = new GridPane();
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);

        usernameField = new TextField();
        passwordField = new PasswordField();
        loginButton = new Button("Login");

        loginGrid.addRow(0, new Label("Username:"), usernameField);
        loginGrid.addRow(1, new Label("Password:"), passwordField);
        loginGrid.addRow(2, loginButton);

        loginButton.setOnAction(e -> handleLogin());

        mainLayout.getChildren().add(loginGrid);
    }

    private void createWeatherSearch() {
        GridPane searchGrid = new GridPane();
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);

        latitudeField = new TextField();
        longitudeField = new TextField();
        radiusField = new TextField();
        searchButton = new Button("Search Weather");
        resultArea = new TextArea();
        resultArea.setEditable(false);

        searchGrid.addRow(0, new Label("Latitude:"), latitudeField);
        searchGrid.addRow(1, new Label("Longitude:"), longitudeField);
        searchGrid.addRow(2, new Label("Search Radius (km):"), radiusField);
        searchGrid.addRow(3, searchButton);

        searchButton.setOnAction(e -> handleWeatherSearch());

        mainLayout.getChildren().addAll(searchGrid, resultArea);
    }

    private void createAdminPanel() {
        HBox adminBox = new HBox(10);
        importButton = new Button("Import JSON Data");
        importButton.setDisable(true);

        importButton.setOnAction(e -> handleJsonImport());

        adminBox.getChildren().add(importButton);
        mainLayout.getChildren().add(adminBox);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password");
            return;
        }

        try {
            LoginRequest loginRequest = new LoginRequest(username, password);
            client.sendRequest(loginRequest);

            // Enable/disable components based on login response
            // This will be handled in the client's response handler
        } catch (Exception e) {
            showAlert("Error", "Login failed: " + e.getMessage());
        }
    }

    private void handleWeatherSearch() {
        try {
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());
            double radius = Double.parseDouble(radiusField.getText());

            Location location = new Location(null,latitude,longitude);
            WeatherRequest weatherRequest = new WeatherRequest(location);
            LocationSearchRequest searchRequest = new LocationSearchRequest(latitude, longitude, radius);

            client.sendRequest(weatherRequest);
            client.sendRequest(searchRequest);
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for latitude, longitude, and radius");
        } catch (Exception e) {
            showAlert("Error", "Search failed: " + e.getMessage());
        }
    }

    private void handleJsonImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select JSON File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                String jsonContent = Files.readString(file.toPath());
                JsonImportRequest importRequest = new JsonImportRequest(jsonContent);
                client.sendRequest(importRequest);
            } catch (Exception e) {
                showAlert("Error", "Failed to import JSON: " + e.getMessage());
            }
        }
    }

    public void handleResponse(Response response) {
        if (response.isSuccess()) {
            if (response.getData() instanceof String) {
                resultArea.setText((String) response.getData());
            } else {
                resultArea.setText(response.getData().toString());
            }

            // If it's a login response and successful, enable the import button
            if (response.getData() instanceof String &&
                    response.getData().toString().contains("Login successful")) {
                importButton.setDisable(false);
            }
        } else {
            showAlert("Error", response.getData().toString());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}