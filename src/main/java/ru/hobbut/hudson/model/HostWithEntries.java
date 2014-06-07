package ru.hobbut.hudson.model;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 14.01.11
 * Time: 21:32
 */

public class HostWithEntries implements Serializable {
    private String hostId;
    private String srcPath;
    private String dstPath;
    private String postBuildScript;
    private boolean enable = true;

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
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

    public String getPostBuildScript() {
        return postBuildScript;
    }

    public void setPostBuildScript(String postBuildScript) {
        this.postBuildScript = postBuildScript;
    }

    @DataBoundConstructor
    public HostWithEntries(String hostId, String srcPath, String dstPath, String postBuildScript, boolean enable) {
        this.hostId = hostId;
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        this.postBuildScript = postBuildScript;
        this.enable = enable;
    }
}
