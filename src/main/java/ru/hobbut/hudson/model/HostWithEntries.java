package ru.hobbut.hudson.model;

import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 14.01.11
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */
public class HostWithEntries {
    private String connectUrl;
    private String srcPath;
    private String dstPath;

    public String getConnectUrl() {
        return connectUrl;
    }

    public void setConnectUrl(String connectUrl) {
        this.connectUrl = connectUrl;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public String getDstPath() {
        return dstPath;
    }

    public void setDstPath(String dstPath) {
        this.dstPath = dstPath;
    }

    @DataBoundConstructor
    public HostWithEntries(String connectUrl, String srcPath, String dstPath) {
        this.connectUrl = connectUrl;
        this.srcPath = srcPath;
        this.dstPath = dstPath;
    }
}
