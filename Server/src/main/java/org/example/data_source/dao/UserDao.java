package org.example.data_source.dao;

import jakarta.persistence.TypedQuery;
import org.example.data_source.Connection;
import org.example.data_source.model.UserEntity;

import java.util.List;
import java.util.Optional;

public class UserDao {
    private Connection connection = new Connection();

    public UserDao(String persistenceUnit) {
        this.connection.initTransaction(persistenceUnit);
    }

    /**
     * Finds all UserEntity records in the database.
     *
     * @return a list of UserEntity
     */
// findAll method
    public List<UserEntity> findAll() {
        return connection.executeReturningTransaction(entityManager -> {
            TypedQuery<UserEntity> query = entityManager.createQuery("SELECT e FROM UserEntity e", UserEntity.class);
            return query.getResultList();
        });
    }
    /**
     * Saves a new UserEntity to the database.
     *
     * @param user the user to save
     */
    public void save(UserEntity user) {
        connection.executeVoidTransaction(entityManager -> entityManager.persist(user));
    }

    public Optional<UserEntity> findByUsernameAndPassword(String username, String password) {
        return connection.executeReturningTransaction(entityManager -> {
            TypedQuery<UserEntity> query = entityManager.createQuery(
                    "SELECT e FROM UserEntity e WHERE e.username = :username AND e.password = :password",
                    UserEntity.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            return query.getResultStream().findFirst();
        });
    }

    public Optional<UserEntity> findByUsername(String username) {
        return connection.executeReturningTransaction(entityManager ->
                entityManager.createQuery("SELECT u FROM UserEntity u WHERE u.username = :username", UserEntity.class)
                        .setParameter("username", username)
                        .getResultStream().findFirst());
    }
}
