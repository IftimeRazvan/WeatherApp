package org.example;

import com.google.gson.Gson;
import org.example.network.Request;
import org.example.data_source.dao.UserDao;
import org.example.data_source.model.UserEntity;
import org.example.data_source.model.WeatherEntity;
import org.example.data_source.model.LocationEntity;
import org.example.service.AuthService;
import org.example.service.WeatherService;
import org.example.data_source.model.Role;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;


public class ClientThread extends Thread {
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final AuthService authService;
    private final WeatherService weatherService;

    public ClientThread(Socket socket,AuthService authService, WeatherService weatherService) {
        this.socket = socket;
        this.authService = authService;
        this.weatherService = weatherService;
        try {
            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            while(true) {
                String message = (String) this.in.readObject();
                System.out.println("SERVER RECEIVED: " + message);
                Request response = execute(message);
                this.out.writeObject(new Gson().toJson(response));  // ✅ Răspunsul este acum trimis către client
                this.out.flush();
                System.out.println("SERVER SENT: " + new Gson().toJson(response));
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Request execute(String message) {
        Request request = new Gson().fromJson(message, Request.class);
        Request response;

        switch (request.getCommandType()){
            case LOGIN -> {
                Optional<UserEntity> user = authService.authenticate(request.getUsername(), request.getPassword());
                UserEntity userEntity = user.get();
                if (user.isPresent()) {
                    Role userRole;
                    if(authService.isAdmin(userEntity)){
                        userRole = Role.ADMIN;
                    }
                    else {
                        userRole = Role.USER;
                    }
                    return response = new Request(request.getUsername(), "", "Login successful!", userRole, request.getCommandType());
                } else {
                     return response = new Request("Server", "", "Invalid credentials.", null, request.getCommandType());
                }
            }
            case REGISTER -> {
                boolean isRegistered = authService.register(request.getUsername(), request.getPassword());
                return response = isRegistered
                        ? new Request(request.getUsername(), "", "User registered successfully!", request.getRole(), request.getCommandType())
                        : new Request("Server", "", "Registration failed! User already exists.", request.getRole(), request.getCommandType());
            }
            case FETCH_WEATHER -> {
                String[] parts = request.getMessage().split(",");
                String locationName = parts[0];
                double latitude = Double.parseDouble(parts[1]);
                double longitude = Double.parseDouble(parts[2]);

                weatherService.addLocationWithoutWeather(locationName, latitude, longitude);

                var weatherData = weatherService.getWeatherByLocation(locationName, latitude, longitude);
                if(weatherData.isPresent()) {
                    WeatherEntity weather = weatherData.get();
                      return response = new Request("Server", "",
                            "Weather Data: " +
                                    "\nTemperature: " + weather.getTemperature() + "°C" +
                                    "\nDescription: " + weather.getWeatherDescription() +
                                    "\nTomorrow's Temperature: " + weather.getTommorowTemperature() + "°C" +
                                    "\nDay After Tomorrow's Temperature: " + weather.getDayAfterTommorowTemperature() + "°C" +
                                    "\nThird Day Temperature: " + weather.getThirdDayTemperature() + "°C",
                            request.getRole(), request.getCommandType());

                }
                else{
                    Optional<LocationEntity> nearestLocation = weatherService.findNearestLocation(latitude, longitude);

                    if (nearestLocation.isPresent()) {
                        var nearestWeatherData = weatherService.getWeatherByLocation(
                                nearestLocation.get().getName(),
                                nearestLocation.get().getLatitude(),
                                nearestLocation.get().getLongitude()
                        );

                        if (nearestWeatherData.isPresent()) {
                            WeatherEntity nearestWeather = nearestWeatherData.get();
                            return response = new Request("Server", "",
                                    "No exact location found. Nearest location: " + nearestLocation.get().getName() +
                                            "\nLatitude: " + nearestLocation.get().getLatitude() +
                                            "\nLongitude: " + nearestLocation.get().getLongitude() +
                                            "\nTemperature: " + nearestWeather.getTemperature() + "°C" +
                                            "\nDescription: " + nearestWeather.getWeatherDescription() +
                                            "\nTomorrow's Temperature: " + nearestWeather.getTommorowTemperature() + "°C" +
                                            "\nDay After Tomorrow's Temperature: " + nearestWeather.getDayAfterTommorowTemperature() + "°C" +
                                            "\nThird Day Temperature: " + nearestWeather.getThirdDayTemperature() + "°C",
                                    request.getRole(), request.getCommandType());
                        }
                        else{
                             return response = new Request("Server", "", "No weather data available for the nearest location.", request.getRole(), request.getCommandType());
                        }
                    }


                }

            }
            case PROVISION_DATA -> {
                String filePath = "src/main/resources/weather_data.json";
                weatherService.importWeatherDataFromFile(filePath);
                return response = new Request("Server", "", "Weather data provisioned successfully from the server file.", request.getRole(), request.getCommandType());
            }
            default -> response = new Request("Server", "", "Unknown command received.", null, request.getCommandType());

        }

        return request;
    }

}
