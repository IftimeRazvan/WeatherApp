package org.example.data_source.model;

import lombok.Data;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name="app_user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Basic
    @Column(name = "username")
    private String username;

    @Basic
    @Column(name = "password")
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_users_roles",
            joinColumns = @JoinColumn(
                    name = "app_user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private List<RoleEntity> roles = new ArrayList<>();

    public void addRole(RoleEntity role) {roles.add(role);}
}
