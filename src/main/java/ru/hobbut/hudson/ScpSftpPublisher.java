package ru.hobbut.hudson;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
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

        private Host findHost(String connectUrl) {
            if (StringUtils.hasText(connectUrl)) {
                return null;
            }
            log.error("finding host:" + connectUrl);
            for (Host host : hosts) {
                log.error("h>>" + host);
                if (connectUrl.equals(host.getConnectUrl())) {
                    return host;
                }
            }
            return null;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            /*JSONArray hosts = formData.getJSONArray("hostsWithEntries");
            log.error("" + formData);
            log.error("" + hosts);
            List<HostWithEntries> hostWithEntriesList = new ArrayList<HostWithEntries>();
            for (Object o : hosts) {
                log.error("" + o);
                String connectUrl = ((JSONObject) o).getString("connectUrl");
                log.error("connUrl>>" + connectUrl);
                JSONObject entriesJson = ((JSONObject) o).getJSONObject("entries");
                List<Entry> entries;
                if (entriesJson != null && !entriesJson.isNullObject()) {
                    entries = req.bindJSON(List.class, entriesJson);
                    log.error("entries>>" + entries);
                } else {
                    entries = new ArrayList<Entry>();
                }
                hostWithEntriesList.add(new HostWithEntries(connectUrl, entries));
            }
            log.error("" + hostWithEntriesList);
            return new ScpSftpPublisher(hostWithEntriesList);*/
            log.error("" + formData);
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
