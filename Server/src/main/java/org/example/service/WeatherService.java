package org.example.service;


import org.example.data_source.dao.WeatherDao;
import org.example.data_source.dao.LocationDao;
import org.example.data_source.model.LocationEntity;
import org.example.data_source.model.WeatherEntity;

import java.util.Optional;
import java.util.List;
import java.util.Comparator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class WeatherService {
    private final WeatherDao weatherDao;
    private final LocationDao locationDao;

    private static final double SEARCH_RADIUS_KM = 50.0;

    public WeatherService(WeatherDao weatherDao, LocationDao locationDao) {
        this.weatherDao = weatherDao;
        this.locationDao = locationDao;
    }


    public void importWeatherDataFromFile(String filePath){
    try {
        String jsonData = new String(Files.readAllBytes(Paths.get(filePath)));
        Gson gson = new Gson();
        Type listType = new TypeToken<List<WeatherEntity>>() {
        }.getType();
        List<WeatherEntity> weatherList = gson.fromJson(jsonData, listType);
        for (WeatherEntity weather : weatherList) {
            Optional<LocationEntity> existingLocation = locationDao.findByDetails(
                    weather.getLocation().getName(),
                    weather.getLocation().getLatitude(),
                    weather.getLocation().getLongitude()
            );

            if (existingLocation.isPresent()) {
                Optional<WeatherEntity> existingWeather = weatherDao.findByLocation(existingLocation.get());

                if (existingWeather.isPresent()) {
                    WeatherEntity weatherToUpdate = existingWeather.get();
                    weatherToUpdate.setTemperature(weather.getTemperature());
                    weatherToUpdate.setWeatherDescription(weather.getWeatherDescription());
                    weatherToUpdate.setTommorowTemperature(weather.getTommorowTemperature());
                    weatherToUpdate.setDayAfterTommorowTemperature(weather.getDayAfterTommorowTemperature());
                    weatherToUpdate.setThirdDayTemperature(weather.getThirdDayTemperature());
                    weatherDao.save(weatherToUpdate);
                } else {
                    weather.setLocation(existingLocation.get());
                    weatherDao.save(weather);
                }
            } else {
                locationDao.save(weather.getLocation());
                weatherDao.save(weather);
            }
        }
    }
    catch (IOException e){}
    }

    public Optional<WeatherEntity> getWeatherByLocation(String locationName,double locationLatitude,double locationLongitude) {
        Optional<LocationEntity> optionalLocation = locationDao.findByDetails(locationName, locationLatitude, locationLongitude);

        if (optionalLocation.isPresent()) {
            return weatherDao.findByLocation(optionalLocation.get());
        }
        return Optional.empty();
    }

    public void addLocationWithoutWeather(String locationName, double latitude, double longitude) {
        if (locationDao.findByDetails(locationName, latitude, longitude).isPresent()) {
            return;
        }

        LocationEntity newLocation = new LocationEntity();
        newLocation.setName(locationName);
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        locationDao.save(newLocation);
    }

    public Optional<LocationEntity> findNearestLocation(double latitude, double longitude) {
        List<LocationEntity> allLocations = locationDao.findAll();

        return allLocations.stream()
                .filter(location ->
                        !(location.getLatitude() == latitude && location.getLongitude() == longitude) &&  // Exclude locația curentă
                                calculateEuclideanDistance(latitude, longitude, location.getLatitude(), location.getLongitude()) <= SEARCH_RADIUS_KM
                )
                .min(Comparator.comparingDouble(location ->
                        calculateEuclideanDistance(latitude, longitude, location.getLatitude(), location.getLongitude())
                ));

    }

    private double calculateEuclideanDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDiff = lat2 - lat1;
        double lonDiff = lon2 - lon1;
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 111;
    }
}
