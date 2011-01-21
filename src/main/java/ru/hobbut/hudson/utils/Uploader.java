package ru.hobbut.hudson.utils;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.FileTransfer;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/21/11
 * Time: 5:54 PM
 */
public class Uploader {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private SSHClient sshClient;
    private PrintStream printStream;
    private FileTransfer fileTransfer;
    private ConnectInfo connectInfo;

    public Uploader(SSHClient sshClient, PrintStream printStream, ConnectInfo connectInfo) {
        this.sshClient = sshClient;
        this.printStream = printStream;
        this.connectInfo = connectInfo;
        switch (connectInfo.getProtocol()) {
            case SCP:
                fileTransfer = sshClient.newSCPFileTransfer();
                break;
            case SFTP:
                try {
                    fileTransfer = sshClient.newSFTPClient().getFileTansfer();
                } catch (IOException e) {
                    throw new PluginException("cannot init sftp transfer", e);
                }
                break;
        }
        fileTransfer.setTransferListener(new MyTransferListener(printStream, connectInfo.getHost()));

    }

    public boolean uploadFile(String localFile, String remotePath) throws IOException {
        File originalFile = new File(localFile);
        String remoteFilePath = FilenameUtils.normalize(connectInfo.getPath() + remotePath);
        remoteFilePath = FilenameUtils.concat(remoteFilePath, originalFile.getName());
        fileTransfer.upload(localFile, remoteFilePath);
        return true;
    }
}
