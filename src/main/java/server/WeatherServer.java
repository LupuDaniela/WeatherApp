
package server;

import service.DatabaseService;
import util.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class WeatherServer {
    private static final Logger logger = Logger.getLogger(WeatherServer.class.getName());
    private ServerSocket serverSocket;
    private DatabaseService databaseService;

    public WeatherServer() {
        this.databaseService = new DatabaseService();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            logger.info("Server started on port " + Constants.SERVER_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, databaseService)).start();
            }
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.severe("Error closing server socket: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new WeatherServer().start();
    }
}
