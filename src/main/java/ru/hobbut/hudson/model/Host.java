package ru.hobbut.hudson.model;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import ru.hobbut.hudson.utils.Utils;

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/13/11
 * Time: 5:26 PM
 */
public class Host implements Serializable {

    private String id;
    private String connectUrl;
    private String credentialsId;
    private boolean enable;

    private transient StandardUsernameCredentials credentials;

    public String getId() {
        return id;
    }

    public String getConnectUrl() {
        return connectUrl;
    }

    public boolean isEnable() {
        return enable;
    }

    @DataBoundConstructor
    public Host(String id, String connectUrl, boolean enable, String credentialsId) {
        this.id = !hasText(id) ? UUID.randomUUID().toString() : id;
        this.connectUrl = connectUrl;
        this.enable = enable;
        this.credentialsId = credentialsId;
    }

    public StandardUsernameCredentials getCredentials() {
        String credentialsId = this.credentialsId == null
                ? (this.credentials == null ? null : this.credentials.getId())
                : this.credentialsId;
        try {
            StandardUsernameCredentials credentials = Utils.lookup(credentialsId);
            if (credentials != null) {
                this.credentials = credentials;
                return credentials;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return credentials;
    }

    private transient String extendedConnectionUrl;
    
    public String getExtendedConnectionUrl() {
        if (hasText(extendedConnectionUrl)) return extendedConnectionUrl;
        StandardUsernameCredentials credentials = getCredentials();
        if (credentials == null) return connectUrl;
        try {
            URI uri = new URI(getConnectUrl());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append(uri.getScheme()).append("://")
                    .append(credentials.getUsername()).append('@')
                    .append(uri.getHost()).append(":")
                    .append(uri.getPort()).append(uri.getPath());
            return extendedConnectionUrl = stringBuilder.toString();
        } catch (Exception e) {
            return connectUrl;
        }
    }


    public String getName() {
        return connectUrl;
    }
}
