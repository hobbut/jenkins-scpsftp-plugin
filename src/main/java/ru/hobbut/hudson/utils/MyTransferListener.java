package ru.hobbut.hudson.utils;

import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.xfer.TransferListener;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/21/11
 * Time: 6:04 PM
 */
public class MyTransferListener implements TransferListener {

    private static final int REPORT_INTERVAL = 10000; //10 sec
    private long currentFileSize = 0;
    private String currentFileName = "";
    private long lastReportDate = 0;
    private PrintStream printStream = null;
    private String host = "";

    public MyTransferListener(PrintStream printStream, String host) {
        this.printStream = printStream;
        this.host = host;
    }

    public TransferListener directory(String name) {
        return new MyTransferListener(printStream, host);
    }

    public StreamCopier.Listener file(String name, long size) {
        currentFileName = name;
        currentFileSize = size;
        return new StreamCopier.Listener() {
            public void reportProgress(long transferred) throws IOException {
                long curDate = new Date().getTime();
                if (printStream != null && lastReportDate + REPORT_INTERVAL < curDate) {
                    lastReportDate = curDate;
                    Utils.logConsole(printStream, String.format("%s: %s %d%%", host, currentFileName, (transferred * 100) / currentFileSize));
                }
            }
        };
    }
}
