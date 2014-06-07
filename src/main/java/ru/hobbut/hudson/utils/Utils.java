package ru.hobbut.hudson.utils;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.SchemeRequirement;
import hudson.security.ACL;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.*;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import net.schmizz.sshj.userauth.password.Resource;
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

import static ru.hobbut.hudson.utils.ConnectInfo.ProtocolType.SCP;
import static ru.hobbut.hudson.utils.ConnectInfo.ProtocolType.SFTP;

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
        ConnectInfo.ProtocolType protocolType;
        try {
            log.debug("connect uri" + host.getConnectUrl());
            URI uri = new URI(host.getConnectUrl());
            String protocol = uri.getScheme();
            if (StringUtils.hasText(protocol)) {
                if ("scp".equalsIgnoreCase(protocol)) {
                    protocolType = SCP;
                } else if ("sftp".equalsIgnoreCase(protocol)) {
                    protocolType = SFTP;
                } else {
                    throw new PluginException("unkown protocol");
                }
            } else {
                protocolType = SCP;
            }

            int port = uri.getPort();
            if (port == -1) {
                port = SSH_PORT;
            }

            return new ConnectInfo(uri.getHost(), port, uri.getPath(), protocolType, host.getCredentials());
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            throw new PluginException("malformed url", e);
        }
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

    private static List<AuthMethod> getAuthMethods(SSHClient client, final ConnectInfo connectInfo) throws Exception {
        List<AuthMethod> authMethods = new ArrayList<AuthMethod>();
        if (connectInfo.getCredentials() instanceof UsernamePasswordCredentials) {
            char[] passwd = Secret.toString(((UsernamePasswordCredentials) connectInfo.getCredentials()).getPassword()).toCharArray().clone();
            authMethods.add(new AuthPassword(PasswordUtils.createOneOff(passwd.clone())));
            authMethods.add(new AuthKeyboardInteractive(new PasswordResponseProvider(PasswordUtils.createOneOff(passwd.clone()))));
        } else if (connectInfo.getCredentials() instanceof SSHUserPrivateKey) {
            String privateKey = ((SSHUserPrivateKey) connectInfo.getCredentials()).getPrivateKey();
            KeyProvider keyProvider = client.loadKeys(privateKey, null, new PasswordFinder() {
                public char[] reqPassword(Resource<?> resource) {
                    return Secret.toString(((SSHUserPrivateKey) connectInfo.getCredentials()).getPassphrase()).toCharArray().clone();
                }

                public boolean shouldRetry(Resource<?> resource) {
                    return false;
                }
            });
            authMethods.add(new AuthPublickey(keyProvider));
        }
        return authMethods;
    }

    public static boolean authenticate(SSHClient sshClient, ConnectInfo connectInfo) {
        try {
            if (connectInfo.getCredentials() instanceof StandardUsernameCredentials) {
                sshClient.auth(((StandardUsernameCredentials) connectInfo.getCredentials()).getUsername(), getAuthMethods(sshClient, connectInfo));
            } else {
                return false;
            }
        } catch (UserAuthException e) {
            log.error(e.getMessage(), e);
            return false;
        } catch (TransportException e) {
            log.error(e.getMessage(), e);
            throw new PluginException("transport error", e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new PluginException("unknown error", e);
        }
        return true;
    }

    public static void disconnectSshClient(SSHClient sshClient) {
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

    public static boolean uploadFile(String localFile, String remotePath, ConnectInfo connectInfo, SSHClient sshClient) throws IOException {
        File originalFile = new File(localFile);
        log.error("remote host path:" + connectInfo.getPath());
        String remoteFilePath = FilenameUtils.normalize(connectInfo.getPath() + remotePath);
        remoteFilePath = FilenameUtils.concat(remoteFilePath, originalFile.getName());
        log.error("full remote path:" + remoteFilePath);

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

        return true;
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

        try {
            uploadFile(localFile, remotePath, connectInfo, sshClient);
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

    public static final SchemeRequirement SSH_SCHEME = new SchemeRequirement("ssh");

    public static StandardUsernameCredentials lookup(String credentialsId) {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider
                        .lookupCredentials(StandardUsernameCredentials.class, Jenkins.getInstance(), ACL.SYSTEM,
                                SSH_SCHEME),
                CredentialsMatchers.withId(credentialsId)
        );
    }
}
