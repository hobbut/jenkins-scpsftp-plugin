package ru.hobbut.hudson.model;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/13/11
 * Time: 5:26 PM
 */
public class Host {

    private String connectUrl;
    private String password;
    private String keyfilePath;

    public String getConnectUrl() {
        return connectUrl;
    }

    public void setConnectUrl(String connectUrl) {
        this.connectUrl = connectUrl;
    }

    public Host() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeyfilePath() {
        return keyfilePath;
    }

    public void setKeyfilePath(String keyfilePath) {
        this.keyfilePath = keyfilePath;
    }

    public Host(String connectUrl, String password) {
        this.connectUrl = connectUrl;
        this.password = password;
    }

    public Host(String connectUrl, String password, String keyfilePath) {
        this.connectUrl = connectUrl;
        this.password = password;
        this.keyfilePath = keyfilePath;
    }

    public String getName() {
        return connectUrl;
    }
}
