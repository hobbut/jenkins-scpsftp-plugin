package ru.hobbut.hudson.model;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/13/11
 * Time: 5:26 PM
 */
public class Host {

    private String connectUrl;
    private String username;
    private String password;

    public String getConnectUrl() {
        return connectUrl;
    }

    public void setConnectUrl(String connectUrl) {
        this.connectUrl = connectUrl;
    }

    public Host() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Host(String connectUrl, String username, String password) {
        this.connectUrl = connectUrl;
        this.username = username;
        this.password = password;
    }
}
