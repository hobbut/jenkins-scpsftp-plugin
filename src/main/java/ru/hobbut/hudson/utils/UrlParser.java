package ru.hobbut.hudson.utils;

import org.springframework.util.StringUtils;
import ru.hobbut.hudson.model.Host;

import java.net.MalformedURLException;
import java.net.URL;

import static ru.hobbut.hudson.utils.ConnectInfo.ProtocolType.*;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/13/11
 * Time: 5:29 PM
 */
public class UrlParser {

    private static final int SSH_PORT = 22;

    public static ConnectInfo getConnectInfo(Host host) {
        ConnectInfo connectInfo = new ConnectInfo();

        try {
            URL url = new URL(host.getConnectUrl());
            String protocol = url.getProtocol();
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


        } catch (MalformedURLException e) {
            throw new PluginException(e);
        }

        return connectInfo;
    }
}
