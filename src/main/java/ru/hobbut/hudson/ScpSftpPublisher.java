package ru.hobbut.hudson;

import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/13/11
 * Time: 5:24 PM
 */
public class ScpSftpPublisher extends Publisher{

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }



}
