package org.example.network;

import com.google.gson.Gson;
import org.example.data_source.model.Role;
import org.example.data_source.CommandType;

public class Request {
    private String username;
    private String password;
    private Role role;
    private String message;
    private CommandType commandType;

    /* TODO: Maybe, add more fields: password, ROLE?, CommandType: Auth, RequestData... */

    public Request() {
        /*EMPTY*/
    }

    public Request(String username,String password, String message,Role role, CommandType commandType) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.message = message;
        this.commandType = commandType;

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CommandType getCommandType() { return commandType; }

    public void setCommandType(CommandType commandType) { this.commandType = commandType; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
