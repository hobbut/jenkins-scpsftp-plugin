package ru.hobbut.hudson.utils;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import ru.hobbut.hudson.model.Host;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static ru.hobbut.hudson.utils.ConnectInfo.ProtocolType.*;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/13/11
 * Time: 5:29 PM
 */
public class Utils {

    private static final int SSH_PORT = 22;

    private static final Log log = LogFactory.getLog(Utils.class);


    public static ConnectInfo getConnectInfo(Host host) {
        ConnectInfo connectInfo = new ConnectInfo();

        try {
            log.debug("connect url" + host.getConnectUrl());
            URI url = new URI(host.getConnectUrl());
            String protocol = url.getScheme();
            if (StringUtils.hasText(protocol)) {
                if ("scp".equalsIgnoreCase(protocol)) {
                    connectInfo.setProtocol(SCP);
                } else if ("sftp".equalsIgnoreCase(protocol)) {
                    connectInfo.setProtocol(SFTP);
                } else {
                    throw new PluginException("wrong protocol");
                }
            } else {
                connectInfo.setProtocol(SCP);
            }

            connectInfo.setHost(url.getHost());

            int port = url.getPort();
            if (port == -1) {
                connectInfo.setPort(SSH_PORT);
            } else {
                connectInfo.setPort(port);
            }

            String path = url.getPath();
            if (StringUtils.hasText(path)) {
                connectInfo.setPath(path);
            }

            connectInfo.setUsername(host.getUsername());
            connectInfo.setPassword(host.getPassword());

        } catch (URISyntaxException e) {
            log.error(e, e);
            throw new PluginException("malformed url", e);
        }

        return connectInfo;
    }

    private static SshAuthenticationClient getAuthenticationClient(ConnectInfo connectInfo) {
        //todo implement more authentications. password only
        SshAuthenticationClient client;
        switch (connectInfo.getAuthType()) {
            case PASSWORD:
                client = new PasswordAuthenticationClient();
                client.setUsername(connectInfo.getUsername());
                ((PasswordAuthenticationClient) client).setPassword(connectInfo.getPassword());
                break;
            default:
                throw new PluginException("unsupported authentication");
        }
        return client;
    }

    private static SshClient getSshClient(ConnectInfo connectInfo) {
        SshClient sshClient = new SshClient();
        sshClient.setSocketTimeout(10000);
        try {
            sshClient.connect(connectInfo.getHost(), connectInfo.getPort(), new IgnoreHostKeyVerification());
        } catch (IOException e) {
            throw new PluginException("can't connect to host", e);
        }
        return sshClient;
    }


    public static boolean checkAuthentication(ConnectInfo connectInfo) {
        SshAuthenticationClient client = getAuthenticationClient(connectInfo);
        SshClient sshClient = getSshClient(connectInfo);
        try {
            int result = sshClient.authenticate(client);
            //todo more log here
            log.error("auth result" + result);
            return result != AuthenticationProtocolState.FAILED && result != AuthenticationProtocolState.CANCELLED;
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e, e);
            }
        }
        sshClient.disconnect();
        return false;
    }
}
