package client;
import model.DailyForecast;
import model.Location;
import model.UserRole;
import model.WeatherData;
import request.*;
import response.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class WeatherClient {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private JTextArea weatherResultArea;
    private String currentUserRole;


    public WeatherClient() {
        try {
            socket = new Socket("localhost", 8080);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            showLoginOrRegister();
            createUI();
            new Thread(this::listenForServerResponses).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error connecting to the server. Ensure the server is running.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherClient::new);
    }

    private void showLoginOrRegister() {
        while (true) {
            String[] options = {"Login", "Register"};
            int choice = JOptionPane.showOptionDialog(null, "Choose an option:", "Login/Register",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                if (performLogin()) {
                    break;
                }
            } else if (choice == 1) {
                performRegister();
            }
        }
    }

    private boolean performLogin() {
        String username = JOptionPane.showInputDialog("Enter Username:");
        String password = JOptionPane.showInputDialog("Enter Password:");

        try {
            sendRequest(new LoginRequest(username, password));
            Object response = input.readObject();

            if (response instanceof Response) {
                Response<?> serverResponse = (Response<?>) response;

                System.out.println("Received response: " + serverResponse);

                if (serverResponse.isSuccess()) {
                    if (serverResponse.getData() instanceof String) {
                        currentUserRole = (String) serverResponse.getData();
                        JOptionPane.showMessageDialog(null, "Login successful! Role: " + currentUserRole);
                        return true;
                    } else {
                        JOptionPane.showMessageDialog(null, "Unexpected response data type: " + serverResponse.getData(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Error: " + serverResponse.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Unexpected response type from server.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Login error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }





    private void performRegister() {
        String username = JOptionPane.showInputDialog("Enter Username:");
        String password = JOptionPane.showInputDialog("Enter Password:");
        String[] roles = {"USER", "ADMIN"};
        String role = (String) JOptionPane.showInputDialog(null, "Select Role:", "Role Selection",
                JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);

        try {
            sendRequest(new RegisterRequest(username, password, role));
            Object response = input.readObject();
            if (response instanceof Response) {
                Response<?> serverResponse = (Response<?>) response;
                if (serverResponse.isSuccess()) {
                    JOptionPane.showMessageDialog(null, "Registration successful! You can now login.");
                } else {
                    JOptionPane.showMessageDialog(null, "Error: " + serverResponse.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Registration error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void createUI() {
    JFrame frame = new JFrame("Weather App - Role: " + currentUserRole);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(400, 400);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


    JLabel searchTypeLabel = new JLabel("Search by:");
    String[] searchOptions = {"Coordinates", "Location Name"};
    JComboBox<String> searchTypeComboBox = new JComboBox<>(searchOptions);
    panel.add(searchTypeLabel);
    panel.add(searchTypeComboBox);


    JLabel latitudeLabel = new JLabel("Latitude:");
    JTextField latitudeField = new JTextField(10);
    JLabel longitudeLabel = new JLabel("Longitude:");
    JTextField longitudeField = new JTextField(10);
    panel.add(latitudeLabel);
    panel.add(latitudeField);
    panel.add(longitudeLabel);
    panel.add(longitudeField);


    JLabel locationNameLabel = new JLabel("Location Name:");
    JTextField locationNameField = new JTextField(20);
    panel.add(locationNameLabel);
    panel.add(locationNameField);

    JLabel weatherResultLabel = new JLabel("Weather Result:");
    weatherResultArea = new JTextArea(10, 30);
    weatherResultArea.setEditable(false);
    panel.add(weatherResultLabel);
    panel.add(new JScrollPane(weatherResultArea));

    JButton fetchWeatherButton = new JButton("Fetch Weather");
    panel.add(fetchWeatherButton);

    fetchWeatherButton.addActionListener(e -> {
        try {
            String searchType = (String) searchTypeComboBox.getSelectedItem();
            if ("Coordinates".equals(searchType)) {
                double latitude = Double.parseDouble(latitudeField.getText());
                double longitude = Double.parseDouble(longitudeField.getText());
                sendRequest(new WeatherRequest(new Location("User Location", latitude, longitude)));
            } else if ("Location Name".equals(searchType)) {
                String locationName = locationNameField.getText();
                sendRequest(new WeatherRequest(new Location(locationName, 0, 0))); // Pass only the location name
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter valid latitude and longitude!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to send request to the server!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    frame.getContentPane().add(panel, BorderLayout.CENTER);
    frame.setVisible(true);

        if ("ADMIN".equals(currentUserRole)) {
            JButton uploadJsonButton = new JButton("Upload JSON Data");
            uploadJsonButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        String jsonData = new String(Files.readAllBytes(selectedFile.toPath()), StandardCharsets.UTF_8);
                        sendRequest(new JsonImportRequest(jsonData));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to read file!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            panel.add(uploadJsonButton);
        }

    }



    private void listenForServerResponses() {
        try {
            while (!socket.isClosed()) {
                Object response = input.readObject();
                handleServerResponse(response);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error listening for server responses: " + e.getMessage());
        }
    }

    private void handleServerResponse(Object response) {
        System.out.println("Received response: " + response);

        if (response instanceof Response) {
            Response<?> serverResponse = (Response<?>) response;

            if (serverResponse.isSuccess() && serverResponse.getData() instanceof WeatherData) {
                WeatherData weatherData = (WeatherData) serverResponse.getData();
                SwingUtilities.invokeLater(() -> {
                    StringBuilder result = new StringBuilder(String.format(
                            "Location: %s (%s, %s)\nTemperature: %.2f°C\nCondition: %s\n\nForecast:\n",
                            weatherData.getLocation().getName(),
                            weatherData.getLocation().getLatitude(),
                            weatherData.getLocation().getLongitude(),
                            weatherData.getTemperature(),
                            weatherData.getCondition()
                    ));


                    for (DailyForecast forecast : weatherData.getForecast()) {
                        result.append(String.format(
                                "Date: %s, Temperature: %.2f°C, Condition: %s\n",
                                forecast.getDate(),
                                forecast.getTemperature(),
                                forecast.getCondition()
                        ));
                    }

                    weatherResultArea.setText(result.toString());
                });
            } else if (!serverResponse.isSuccess()) {
                JOptionPane.showMessageDialog(null, "Error: " + serverResponse.getMessage(), "Server Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.err.println("Unknown response type from server.");
        }
    }


    private void sendRequest(Object request) throws IOException {
        System.out.println("Sending request: " + request);
        output.writeObject(request);
        output.flush();
    }
}
