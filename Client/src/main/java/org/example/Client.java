package org.example;

import com.google.gson.Gson;
import com.google.gson.Gson;
import org.example.Request;
import org.example.CommandType;
import org.example.Role;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final int PORT = 6543;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private String password;
    private Role role;
    private String currentLocation;
    private boolean authenticated = false;

    public void start() {
        try {
            Socket socket = new Socket("localhost", PORT);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);

            /* Write data to the server */
            new Thread(() -> {
                try {
                    while (!authenticated) {
                        System.out.println("Choose an option:");
                        System.out.println("1. Login");
                        System.out.println("2. Register");
                        System.out.println("3. Exit");
                        int choice;
                        try {
                            choice = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid choice. Please enter 1,2 or 3.");
                            continue;
                        }

                        if (choice == 3) {
                            System.out.println("Exiting the application...");
                            socket.close();
                            System.exit(0);
                        }


                        System.out.println("Insert your username: ");
                        username = scanner.nextLine();
                        System.out.println("Insert your password: ");
                        password = scanner.nextLine();

                        Request request = (choice == 1)
                                ? new Request(username, password, "", null, CommandType.LOGIN)
                                : new Request(username, password, "", null, CommandType.REGISTER);

                        sendRequest(request);

                        synchronized (this) {
                            while (!authenticated) {
                                this.wait();
                            }
                        }
                    }


                    while (true) {
                        System.out.println("\nMenu:");
                        System.out.println("1. Set Location");
                        System.out.println("2. Fetch Weather Data");
                        if (role == Role.ADMIN) {
                            System.out.println("3. Provision Data");
                        }
                        System.out.println("4. Exit");

                        int choice = Integer.parseInt(scanner.nextLine());

                        switch (choice) {
                            case 1 -> setLocation(scanner);
                            case 2 -> fetchWeather();
                            case 3 -> {
                                if (role == Role.ADMIN) {
                                    Request provisionDataRequest = new Request(username, password, "", role, CommandType.PROVISION_DATA);
                                    sendRequest(provisionDataRequest);
                                } else {
                                    System.out.println("You do not have permission to provision data.");
                                }
                            }
                            case 4 -> {
                                System.out.println("Exiting...");
                                socket.close();
                                System.exit(0);
                            }
                            default -> System.out.println("Invalid option. Please try again.");
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    System.err.println("Error during communication with server: " + e.getMessage());
                }
            }).start();

            /* Read data from the server */

            new Thread(() -> {
                try {
                    while (true) {
                        //synchronized (this) {
                            String serverData = (String) this.in.readObject();
                            Request response = new Gson().fromJson(serverData, Request.class);

                            switch (response.getCommandType()) {
                                case LOGIN, REGISTER -> {
                                    synchronized (this) {
                                        if (response.getMessage().contains("Login successful!") || response.getMessage().contains("User registered successfully!")) {
                                            this.authenticated = true;
                                            this.role = response.getRole();
                                        } else {
                                            this.authenticated = false;
                                        }
                                        System.out.println(response.getMessage());
                                        this.notifyAll();
                                    }
                                }
                                case FETCH_WEATHER, PROVISION_DATA -> {
                                    System.out.println(response.getMessage());
                                }
                                default -> System.out.println("Server Response: " + response.getMessage());
                            }
                        }
                    //}
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {}
    }


    private void setLocation(Scanner scanner) {
        System.out.println("Enter your location: ");
        currentLocation = scanner.nextLine();
        System.out.println("Location set successfully to: " + currentLocation);
    }

    private void fetchWeather() throws IOException {
        if (currentLocation == null || currentLocation.isEmpty()) {
            System.out.println("You need to set your location first using 'Set Location' option.");
            return;
        }
        Request fetchWeatherRequest = new Request(username, password, currentLocation, role, CommandType.FETCH_WEATHER);
        sendRequest(fetchWeatherRequest);
    }

    private void sendRequest(Request request) throws IOException {
        String requestJson = new Gson().toJson(request);
        this.out.writeObject(requestJson);
        this.out.flush();
    }

    private synchronized void waitForAuthenticationResponse() {
        try {
            while (!authenticated) {
                System.out.println("Waiting for server response...");
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
