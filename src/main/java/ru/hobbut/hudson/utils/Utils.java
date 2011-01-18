package ru.hobbut.hudson.utils;

import com.sshtools.j2ssh.ScpClient;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import ru.hobbut.hudson.model.Host;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

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

    public static final String LOG_PREFIX = "[SCPSFTP] ";


    public static ConnectInfo getConnectInfo(Host host) {
        ConnectInfo connectInfo = new ConnectInfo();

        try {
            log.debug("connect uri" + host.getConnectUrl());
            URI uri = new URI(host.getConnectUrl());
            String protocol = uri.getScheme();
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

            connectInfo.setHost(uri.getHost());

            int port = uri.getPort();
            if (port == -1) {
                connectInfo.setPort(SSH_PORT);
            } else {
                connectInfo.setPort(port);
            }

            String path = uri.getPath();
            if (StringUtils.hasText(path)) {
                connectInfo.setPath(path);
            }

            connectInfo.setUsername(uri.getUserInfo());
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


    private static boolean authenticate(SshClient sshClient, SshAuthenticationClient sshAuthenticationClient) throws IOException {
        int result = sshClient.authenticate(sshAuthenticationClient);
        log.error("auth result" + result);
        return result == AuthenticationProtocolState.COMPLETE;
    }

    public static boolean checkAuthentication(ConnectInfo connectInfo) {
        SshAuthenticationClient client = getAuthenticationClient(connectInfo);
        SshClient sshClient = getSshClient(connectInfo);
        try {
            return authenticate(sshClient, client);
        } catch (IOException e) {
            return false;
        } finally {
            sshClient.disconnect();
        }
    }

    public static boolean uploadFile(String localFile, String remotePath, Host host) {

        ConnectInfo connectInfo = getConnectInfo(host);
        SshClient sshClient = getSshClient(connectInfo);
        SshAuthenticationClient client = getAuthenticationClient(connectInfo);
        //auth first
        try {
            if (!authenticate(sshClient, client)) {
                log.error("auth failed");
                return false;
            }
        } catch (IOException e) {
            log.error("auth failed");
            return false;
        }

        File originalFile = new File(localFile);
        log.error("remote host path:" + connectInfo.getPath());
        String remoteFilePath = FilenameUtils.normalize(connectInfo.getPath() + remotePath);
        remoteFilePath = FilenameUtils.concat(remoteFilePath, originalFile.getName());

        log.error("full remote path:" + remoteFilePath);

        try {
            switch (connectInfo.getProtocol()) {
                case SCP: {
                    ScpClient scpClient = sshClient.openScpClient();
                    scpClient.put(localFile, remoteFilePath, originalFile.isDirectory());
                    break;
                }
                case SFTP: {
                    SftpClient sftpClient = sshClient.openSftpClient();
                    sftpClient.put(localFile, remoteFilePath);
                    break;
                }
            }

        } catch (IOException e) {
            return false;
        } finally {
            sshClient.disconnect();
        }

        return true;
    }

    public static void logConsole(PrintStream logger, String message) {
        logger.println("" + LOG_PREFIX + message);
    }
}
