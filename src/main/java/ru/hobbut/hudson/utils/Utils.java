package ru.hobbut.hudson.utils;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.*;
import net.schmizz.sshj.userauth.method.*;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import ru.hobbut.hudson.model.Host;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static ru.hobbut.hudson.utils.ConnectInfo.ProtocolType.*;
import static ru.hobbut.hudson.utils.ConnectInfo.AuthType.*;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/13/11
 * Time: 5:29 PM
 */
public class Utils {

    private static final int SSH_PORT = 22;

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static final String LOG_PREFIX = "[SCPSFTP] ";
    private static final int CONNECT_TIMEOUT = 10000;

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

            if (StringUtils.hasText(host.getKeyfilePath())) {
                connectInfo.setKeyfilePath(host.getKeyfilePath());
                connectInfo.setAuthType(KEYFILE);
            }

            connectInfo.setUsername(uri.getUserInfo());
            connectInfo.setPassword(host.getPassword());

        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            throw new PluginException("malformed url", e);
        }

        return connectInfo;
    }

    public static SSHClient getSshClient(ConnectInfo connectInfo) {
        SSHClient sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        sshClient.setTimeout(CONNECT_TIMEOUT);
        try {
            sshClient.connect(connectInfo.getHost(), connectInfo.getPort());
        } catch (IOException e) {
            throw new PluginException("can't connect to host", e);
        }
        return sshClient;
    }

    private static List<AuthMethod> getAuthMethods(SSHClient client, final ConnectInfo connectInfo) {
        List<AuthMethod> authMethods = new ArrayList<AuthMethod>();
        switch (connectInfo.getAuthType()) {
            case PASSWORD:
                char[] passwd = connectInfo.getPassword().toCharArray();
                authMethods.add(new AuthPassword(PasswordUtils.createOneOff(passwd.clone())));
                authMethods.add(new AuthKeyboardInteractive(new PasswordResponseProvider(PasswordUtils.createOneOff(passwd.clone()))));
                break;
            case KEYFILE:
                KeyProvider keyProvider;
                try {
                    keyProvider = client.loadKeys(connectInfo.getKeyfilePath(), connectInfo.getPassword());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    throw new PluginException("error reading keyfile");
                }
                AuthPublickey authPublickey = new AuthPublickey(keyProvider);
                authMethods.add(authPublickey);
                break;
            default:
                throw new PluginException("unsupported authentication");
        }
        return authMethods;
    }

    private static boolean authenticate(SSHClient sshClient, ConnectInfo connectInfo) {
        try {
            sshClient.auth(connectInfo.getUsername(), getAuthMethods(sshClient, connectInfo));
        } catch (UserAuthException e) {
            log.error(e.getMessage(), e);
            return false;
        } catch (TransportException e) {
            throw new PluginException("transport error", e);
        }
        return true;
    }

    private static void disconnectSshClient(SSHClient sshClient) {
        try {
            sshClient.disconnect();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static boolean checkAuthentication(ConnectInfo connectInfo) {
        SSHClient sshClient = getSshClient(connectInfo);
        try {
            return authenticate(sshClient, connectInfo);
        } finally {
            disconnectSshClient(sshClient);
        }
    }

    public static boolean uploadFile(String localFile, String remotePath, Host host) {
        ConnectInfo connectInfo = getConnectInfo(host);
        SSHClient sshClient = getSshClient(connectInfo);

        //auth first
        if (!authenticate(sshClient, connectInfo)) {
            log.error("auth failed");
            disconnectSshClient(sshClient);
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
                    sshClient.newSCPFileTransfer().upload(localFile, remoteFilePath);
                    break;
                }
                case SFTP: {
                    sshClient.newSFTPClient().put(localFile, remoteFilePath);
                    break;
                }
            }

        } catch (IOException e) {
            return false;
        } finally {
            disconnectSshClient(sshClient);
        }
        return true;
    }

    public static void logConsole(PrintStream logger, String message) {
        logger.println("" + LOG_PREFIX + message);
    }
}
