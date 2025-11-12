package org.example.data_source.dao;

import org.example.data_source.Connection;
import org.example.data_source.model.WeatherEntity;
import org.example.data_source.model.LocationEntity;

import java.util.List;
import java.util.Optional;

public class WeatherDao {
    private Connection connection = new Connection();

    public WeatherDao(String persistenceUnit) {
        this.connection.initTransaction(persistenceUnit);
    }

    public List<WeatherEntity> findAll() {
        return connection.executeReturningTransaction(entityManager ->
                entityManager.createQuery("SELECT w FROM WeatherEntity w", WeatherEntity.class).getResultList());
    }

    public void save(WeatherEntity weather) {
        connection.executeVoidTransaction(entityManager -> entityManager.persist(weather));
    }

    public void update(WeatherEntity weather) {
        connection.executeVoidTransaction(entityManager -> entityManager.merge(weather));
    }

    public Optional<WeatherEntity> findByLocation(LocationEntity location) {
        return connection.executeReturningTransaction(entityManager ->
                entityManager.createQuery("SELECT w FROM WeatherEntity w WHERE w.location = :location", WeatherEntity.class)
                        .setParameter("location", location)
                        .getResultStream().findFirst());
    }
}
