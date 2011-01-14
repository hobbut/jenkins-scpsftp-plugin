package ru.hobbut.hudson.utils;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/13/11
 * Time: 5:38 PM
 */
public class ConnectInfo {

    public static enum ProtocolType {
        SCP, SFTP
    }

    public static enum AuthType {
        PASSWORD,
    }

    ConnectInfo() {
    }

    private String host;
    private int port;
    private String path;
    private ProtocolType protocol;
    private String username;
    private String password;
    private AuthType authType = AuthType.PASSWORD;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
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

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectInfo that = (ConnectInfo) o;

        if (port != that.port) return false;
        if (authType != that.authType) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (protocol != that.protocol) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (authType != null ? authType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConnectInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", path='" + path + '\'' +
                ", protocol=" + protocol +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", authType=" + authType +
                '}';
    }
}
