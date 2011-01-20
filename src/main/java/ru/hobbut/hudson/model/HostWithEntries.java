package ru.hobbut.hudson.model;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 14.01.11
 * Time: 21:32
 */

public class HostWithEntries {
    private String connectUrl;
    private String srcPath;
    private String dstPath;
    private boolean enable = true;

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

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @DataBoundConstructor
    public HostWithEntries(String connectUrl, String srcPath, String dstPath, boolean enable) {
        this.connectUrl = connectUrl;
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        this.enable = enable;
    }
}
