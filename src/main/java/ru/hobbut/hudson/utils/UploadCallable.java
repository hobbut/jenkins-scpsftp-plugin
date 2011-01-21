package ru.hobbut.hudson.utils;

import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import net.schmizz.sshj.SSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hobbut.hudson.model.Host;
import ru.hobbut.hudson.model.HostWithEntries;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static ru.hobbut.hudson.utils.Utils.getConnectInfo;
import static ru.hobbut.hudson.utils.Utils.logConsole;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/20/11
 * Time: 8:14 PM
 */

public class UploadCallable implements Callable<Map<HostWithEntries, Boolean>> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private List<HostWithEntries> hostsWithEntries;
    private Host host;
    private AbstractBuild build;
    private BuildListener listener;


    public UploadCallable(List<HostWithEntries> hostsWithEntries, Host host, AbstractBuild build, BuildListener listener) {
        this.hostsWithEntries = hostsWithEntries;
        this.host = host;
        this.build = build;
        this.listener = listener;
    }

    public Map<HostWithEntries, Boolean> call() throws Exception {

        Map<HostWithEntries, Boolean> map = new HashMap<HostWithEntries, Boolean>();

        if (!host.isEnable()) {
            logConsole(listener.getLogger(), "Skipping disabled host:" + host.getConnectUrl());
            for (HostWithEntries hostWithEntries : hostsWithEntries) {
                map.put(hostWithEntries, true);
            }
            return map;
        }

        ConnectInfo connectInfo = getConnectInfo(host);
        SSHClient sshClient = Utils.getSshClient(connectInfo);
        if (!Utils.authenticate(sshClient, connectInfo)) {
            Utils.disconnectSshClient(sshClient);
            logConsole(listener.getLogger(), "Error authenticating on " + host.getConnectUrl());
            for (HostWithEntries hostWithEntries : hostsWithEntries) {
                map.put(hostWithEntries, false);
            }
            return map;
        }

        Uploader uploader = new Uploader(sshClient, listener.getLogger(), connectInfo);

        for (HostWithEntries hostWithEntries : hostsWithEntries) {

            String expandedSrcPath = Util.replaceMacro(hostWithEntries.getSrcPath(), build.getEnvironment(listener)).trim();
            String expandedDstPath = Util.replaceMacro(hostWithEntries.getDstPath(), build.getEnvironment(listener)).trim();
            FilePath ws = build.getWorkspace();
            FilePath[] src = ws.list(expandedSrcPath);

            if (src.length == 0) {
                logConsole(listener.getLogger(), "No files found at:" + hostWithEntries.getSrcPath());
                continue;
            }
            boolean res = true;
            for (FilePath filePath : src) {
                try{
                    res = uploader.uploadFile(filePath.getRemote(), expandedDstPath);
                } catch (IOException e) {
                    res = false;
                }
                if (res) {
                    logConsole(listener.getLogger(), String.format("Successfully uploaded %s to %s",
                            filePath.getRemote(), host.getConnectUrl()));
                } else {
                    logConsole(listener.getLogger(), String.format("Error upload %s to %s", filePath.getRemote(),
                            host.getConnectUrl()));
                }
            }

            map.put(hostWithEntries, res);
        }
        Utils.disconnectSshClient(sshClient);
        return map;
    }

}
