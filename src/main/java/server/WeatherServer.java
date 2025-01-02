package server;

import service.DatabaseService;
import util.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WeatherServer {
    private ServerSocket serverSocket;
    private DatabaseService databaseService;

    public WeatherServer() {
        this.databaseService=new DatabaseService();
    }
    public void start() {
        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            System.out.println("Server started on port" + Constants.SERVER_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, databaseService)).start();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new WeatherServer().start();
    }
}
