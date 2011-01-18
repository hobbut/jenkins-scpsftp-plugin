package ru.hobbut.hudson;

import hudson.*;
import hudson.model.*;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.springframework.util.StringUtils;
import ru.hobbut.hudson.model.Host;
import ru.hobbut.hudson.model.HostWithEntries;
import ru.hobbut.hudson.utils.ConnectInfo;
import ru.hobbut.hudson.utils.PluginException;
import ru.hobbut.hudson.utils.Utils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/13/11
 * Time: 5:24 PM
 */
public class ScpSftpPublisher extends Publisher {

    private static final Log log = LogFactory.getLog(ScpSftpPublisher.class);

    private List<HostWithEntries> hostsWithEntries;

    @DataBoundConstructor
    public ScpSftpPublisher(List<HostWithEntries> hostsWithEntries) {
        if (hostsWithEntries == null) {
            hostsWithEntries = new ArrayList<HostWithEntries>();
        }
        this.hostsWithEntries = hostsWithEntries;
    }

    public List<HostWithEntries> getHostsWithEntries() {
        return hostsWithEntries;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    private Host getHost(String connectUrl) {
        return DESCRIPTOR.getHost(connectUrl);
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        if (build.getResult() == Result.FAILURE) {
            // build failed. don't post
            return true;
        }

        Result result = Result.SUCCESS;

        for (HostWithEntries hostWithEntries : hostsWithEntries) {
            logConsole(listener.getLogger(), "proceed:" + hostWithEntries.getConnectUrl());
            try {
                Host host = getHost(hostWithEntries.getConnectUrl());
                if (host == null) {
                    build.setResult(Result.UNSTABLE);
                    logConsole(listener.getLogger(), "Cannot find host:" + hostWithEntries.getConnectUrl());
                    continue;
                }
                String expandedSrcPath = Util.replaceMacro(hostWithEntries.getSrcPath(), build.getEnvironment(listener));
                String expandedDstPath = Util.replaceMacro(hostWithEntries.getDstPath(), build.getEnvironment(listener)).trim();
                FilePath ws = build.getWorkspace();
                FilePath[] src = ws.list(expandedSrcPath);

                if (src.length == 0) {
                    logConsole(listener.getLogger(), "no files found at:" + hostWithEntries.getSrcPath());
                    continue;
                }

                for (FilePath filePath : src) {
                    if (!Utils.uploadFile(filePath.getRemote(), expandedDstPath, host)) {
                        result = Result.UNSTABLE;
                        logConsole(listener.getLogger(), String.format("Error upload %s to %s", filePath.getRemote(),
                                host.getConnectUrl()));
                    } else {
                        logConsole(listener.getLogger(), String.format("Successfully uploaded %s to %s",
                                filePath.getRemote(), host.getConnectUrl()));
                    }
                }


            } catch (InterruptedException e) {
                e.printStackTrace(listener.error("error"));  //To change body of catch statement use File | Settings | File Templates.
                result = Result.UNSTABLE;
            } catch (IOException e) {
                e.printStackTrace(listener.error("error"));  //To change body of catch statement use File | Settings | File Templates.
                result = Result.UNSTABLE;
            }
        }
        build.setResult(result);
        return true;
    }

    @Extension // this marker indicates Hudson that this is an implementation of an extension point.
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private final CopyOnWriteList<Host> hosts = new CopyOnWriteList<Host>();

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "SCP SFTP Publisher";
        }

        private Host findHost(String connectUrl) {
            if (StringUtils.hasText(connectUrl)) {
                return null;
            }

            for (Host host : hosts) {
                if (connectUrl.equals(host.getConnectUrl())) {
                    return host;
                }
            }
            return null;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(ScpSftpPublisher.class, formData);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            hosts.replaceBy(req.bindParametersToList(Host.class, "scp."));
            save();
            return true;
        }

        public DescriptorImpl(Class<? extends Publisher> clazz) {
            super(clazz);
        }

        public DescriptorImpl() {
            load();
        }

        public Host getHost(String connectionUrl) {
            if (!StringUtils.hasText(connectionUrl)) {
                return null;
            }
            for (Host host : hosts) {
                if (connectionUrl.equals(host.getConnectUrl())) {
                    return host;
                }
            }
            return null;
        }

        public Host[] getHosts() {
            return hosts.toArray(new Host[hosts.size()]);
        }

        public FormValidation doTestConnection(StaplerRequest req, StaplerResponse rsp,
                                               @QueryParameter("scp.connectUrl") String connectUrl,
                                               @QueryParameter("scp.password") String password) {
            Host host = new Host(connectUrl, password);
            try {
                ConnectInfo connectInfo = Utils.getConnectInfo(host);
                return Utils.checkAuthentication(connectInfo) ? FormValidation.ok("Connection ok") : FormValidation.error("Authentication failed");
            } catch (PluginException e) {
                return FormValidation.error(e.getMessage(), e);
            }
        }
    }

    public static void logConsole(PrintStream logger, String message) {
        Utils.logConsole(logger, message);
    }
}
