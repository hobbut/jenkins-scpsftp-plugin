package ru.hobbut.hudson;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import ru.hobbut.hudson.model.Host;
import ru.hobbut.hudson.utils.ConnectInfo;
import ru.hobbut.hudson.utils.PluginException;
import ru.hobbut.hudson.utils.Utils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/13/11
 * Time: 5:24 PM
 */
public class ScpSftpPublisher extends Publisher {

    private static final Log log = LogFactory.getLog(ScpSftpPublisher.class);

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Extension // this marker indicates Hudson that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private final CopyOnWriteList<Host> hosts = new CopyOnWriteList<Host>();

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "SCP SFTP OLOLO";
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
            load();
            log.debug(hosts);
        }

        public DescriptorImpl() {
            load();
            log.debug(hosts);
        }

        public Host[] getHosts() {
            Iterator<Host> it = hosts.iterator();
            int size = 0;
            while (it.hasNext()) {
                it.next();
                size++;
            }
            return hosts.toArray(new Host[size]);
        }

        public FormValidation doTestConnection(StaplerRequest req, StaplerResponse rsp,
                                               @QueryParameter("scp.connectUrl") String connectUrl,
                                               @QueryParameter("scp.username") final String username,
                                               @QueryParameter("scp.password") final String password) {
            log.error("" + req.getParameterMap());
            Host host = new Host(connectUrl, username, password);
            log.error("" + connectUrl);
            try {
                ConnectInfo connectInfo = Utils.getConnectInfo(host);
                return Utils.checkAuthentication(connectInfo) ? FormValidation.ok("Connection ok") : FormValidation.error("Authentication failed");
            } catch (PluginException e) {
                return FormValidation.error(e.getMessage(), e);
            }
        }
    }
}
