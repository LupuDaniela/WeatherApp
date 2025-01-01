import javafx.application.Application;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class WeatherClient extends Application {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private WeatherUI ui;

    @Override
    public void start(Stage primaryStage) {
        try {
            socket = new Socket("localhost", Constants.SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            ui = new WeatherUI(this);
            ui.start(primaryStage);

            new Thread(this::listenForServerResponses).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForServerResponses() {
        try {
            while (true) {
                Object response = input.readObject();
                handleServerResponse(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleServerResponse(Object response) {
        // Implement response handling logic
    }

    public static void main(String[] args) {
        launch(args);
    }
}