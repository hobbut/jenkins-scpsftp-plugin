package ru.hobbut.hudson.utils;

import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import ru.hobbut.hudson.model.Host;
import ru.hobbut.hudson.model.HostWithEntries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static ru.hobbut.hudson.utils.Utils.logConsole;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/20/11
 * Time: 8:14 PM
 */

public class UploadCallable implements Callable<Map<HostWithEntries, Boolean>> {

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

        for (HostWithEntries hostWithEntries : hostsWithEntries) {
            boolean result = true;
            if (!host.isEnable()) {
                logConsole(listener.getLogger(), "Skipping disabled host:" + host.getConnectUrl());
                continue;
            }

            String expandedSrcPath = Util.replaceMacro(hostWithEntries.getSrcPath(), build.getEnvironment(listener)).trim();
            String expandedDstPath = Util.replaceMacro(hostWithEntries.getDstPath(), build.getEnvironment(listener)).trim();
            FilePath ws = build.getWorkspace();
            FilePath[] src = ws.list(expandedSrcPath);

            if (src.length == 0) {
                logConsole(listener.getLogger(), "No files found at:" + hostWithEntries.getSrcPath());
                continue;
            }

            for (FilePath filePath : src) {
                if (!Utils.uploadFile(filePath.getRemote(), expandedDstPath, host)) {
                    result = false;
                    logConsole(listener.getLogger(), String.format("Error upload %s to %s", filePath.getRemote(),
                            host.getConnectUrl()));
                } else {
                    logConsole(listener.getLogger(), String.format("Successfully uploaded %s to %s",
                            filePath.getRemote(), host.getConnectUrl()));
                }
            }

            map.put(hostWithEntries, result);
        }
        return map;
    }

}
