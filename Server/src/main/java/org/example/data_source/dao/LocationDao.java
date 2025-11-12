package org.example.data_source.dao;

import org.example.data_source.Connection;
import org.example.data_source.model.LocationEntity;

import java.util.List;
import java.util.Optional;

public class LocationDao {
    private Connection connection = new Connection();

    public LocationDao(String persistenceUnit) {
        this.connection.initTransaction(persistenceUnit);
    }

    public List<LocationEntity> findAll() {
        return connection.executeReturningTransaction(entityManager ->
                entityManager.createQuery("SELECT l FROM LocationEntity l", LocationEntity.class).getResultList()
        );
    }

    public void save(LocationEntity location) {
        connection.executeVoidTransaction(entityManager -> entityManager.persist(location));
    }

    public Optional<LocationEntity> findByDetails(String name, double latitude, double longitude) {
        return connection.executeReturningTransaction(entityManager ->
                entityManager.createQuery(
                                "SELECT l FROM LocationEntity l WHERE l.name = :name AND l.latitude = :latitude AND l.longitude = :longitude",
                                LocationEntity.class)
                        .setParameter("name", name)
                        .setParameter("latitude", latitude)
                        .setParameter("longitude", longitude)
                        .getResultStream()
                        .findFirst());
    }
}
