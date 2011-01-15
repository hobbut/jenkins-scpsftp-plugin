package ru.hobbut.hudson.model;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 14.01.11
 * Time: 21:31
 * To change this template use File | Settings | File Templates.
 */
public class Entry {
    private String srcPath;
    private String dstPath;

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

    public Entry(String srcPath, String dstPath) {
        this.srcPath = srcPath;
        this.dstPath = dstPath;
    }
}
