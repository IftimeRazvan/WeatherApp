package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.example.service.AuthService;
import org.example.service.WeatherService;
import org.example.data_source.dao.LocationDao;
import org.example.data_source.dao.UserDao;
import org.example.data_source.dao.WeatherDao;
import org.example.data_source.Connection;

public class Server {
    private final int SERVER_PORT = 6543;
    private final AuthService authService = new AuthService(new UserDao("postgresPersistence"),new Connection("postgresPersistence"));
    private final WeatherService weatherService = new WeatherService(new WeatherDao("postgresPersistence"), new LocationDao("postgresPersistence"));
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            while(true) {
                Socket client = serverSocket.accept();
                new ClientThread(client,authService,weatherService).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
