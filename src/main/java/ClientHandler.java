import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DatabaseService databaseService;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public ClientHandler(Socket socket,DatabaseService databaseService) {
        this.clientSocket = socket;
        this.databaseService = databaseService;
    }

    @Override
    public void run() {
        try {
            input=new ObjectInputStream(clientSocket.getInputStream());
            output=new ObjectOutputStream(clientSocket.getOutputStream());
            while(true) {
                Object request=input.readObject();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Object request) {

    }
}
