package org.example.service;

import org.example.data_source.dao.UserDao;

import org.example.data_source.Connection;
import org.example.data_source.model.UserEntity;
import org.example.data_source.model.RoleEntity;
import org.example.data_source.model.Role;

import java.util.Optional;

public class AuthService {
    private final UserDao userDao;
    private final Connection connection;

    private static RoleEntity ADMIN_ROLE;
    private static RoleEntity USER_ROLE;

    public AuthService(UserDao userDao,Connection connection) {
        this.userDao = userDao;
        this.connection = connection;
        initializeRoles();
    }

    private void initializeRoles() {
        ADMIN_ROLE = createOrGetRole(Role.ADMIN);
        USER_ROLE = createOrGetRole(Role.USER);
    }

    private RoleEntity createOrGetRole(Role role) {
        Optional<RoleEntity> existingRole = connection.executeReturningTransaction(entityManager ->
                entityManager.createQuery("SELECT r FROM RoleEntity r WHERE r.name = :name", RoleEntity.class)
                        .setParameter("name", role.name())
                        .getResultStream()
                        .findFirst());

        if (existingRole.isPresent()) {
            return existingRole.get();
        } else {
            RoleEntity newRole = new RoleEntity();
            newRole.setRole(role);
            connection.executeVoidTransaction(entityManager -> entityManager.persist(newRole));
            return newRole;
        }
    }


    public Optional<UserEntity> authenticate(String username, String password) {
        return userDao.findByUsernameAndPassword(username, password);
    }

    public boolean register(String username, String password) {

        boolean isFirstUser = userDao.findAll().isEmpty();

        Optional<UserEntity> existingUser = userDao.findByUsername(username);
        if (existingUser.isPresent()) {
            return false;
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);
        newUser.setPassword(password);


        newUser.addRole(USER_ROLE);
        USER_ROLE.addUser(newUser);

        if (isFirstUser) {
            newUser.addRole(ADMIN_ROLE);
            ADMIN_ROLE.addUser(newUser);
        }

        userDao.save(newUser);
        return true;
    }

    public boolean isAdmin(UserEntity user) {
        return user.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("ADMIN"));
    }
}
