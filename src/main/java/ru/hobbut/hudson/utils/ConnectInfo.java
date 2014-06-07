package ru.hobbut.hudson.utils;

import com.cloudbees.plugins.credentials.Credentials;

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

    private final String host;
    private final int port;
    private final String path;
    private final ProtocolType protocol;
    private final Credentials credentials;

    public ConnectInfo(String host, int port, String path, ProtocolType protocol, Credentials credentials) {
        this.host = host;
        this.port = port;
        this.path = path;
        this.protocol = protocol;
        this.credentials = credentials;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectInfo that = (ConnectInfo) o;

        if (port != that.port) return false;
        if (credentials != null ? !credentials.equals(that.credentials) : that.credentials != null) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (protocol != that.protocol) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (credentials != null ? credentials.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectInfo{");
        sb.append("host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", path='").append(path).append('\'');
        sb.append(", protocol=").append(protocol);
        sb.append(", credentials=").append(credentials);
        sb.append('}');
        return sb.toString();
    }
}
