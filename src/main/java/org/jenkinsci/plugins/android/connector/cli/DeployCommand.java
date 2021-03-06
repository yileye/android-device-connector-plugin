package org.jenkinsci.plugins.android.connector.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.cli.CLICommand;
import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.android.connector.AndroidDeviceList;
import org.jenkinsci.plugins.android.connector.AndroidDevice;
import org.kohsuke.args4j.Argument;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

/**
 * CLI command to deploy IPA files.
 */
@Extension
public class DeployCommand extends CLICommand {
    @Argument(metaVar="DEVICEID",
        usage="Unique device ID or device name",required=true)
    public String device;

    @Argument(index=1,metaVar="BUNDLE",usage="*.apk file(s) to deploy",required=true)
    public List<String> files;

    @Inject
    AndroidDeviceList devices;

    @Override
    public String getName() {
        return "android-deploy-apk";
    }

    @Override
    public String getShortDescription() {
        return "Deploy apps to Android devices connected to Jenkins";
    }

    @Override
    protected int run() throws Exception {
        Jenkins.getInstance().getInjector().injectMembers(this);

        AndroidDevice dev = devices.getDevice(device);
        if (dev==null)
            throw new AbortException("No such device found: "+device);

        TaskListener listener = new StreamTaskListener(stdout,getClientCharset());
        for (String bundle : files) {
            FilePath p = new FilePath(checkChannel(), bundle);
            listener.getLogger().println("Deploying "+ bundle);
            dev.deploy(new File(p.getRemote()), listener);
        }
        return 0;
    }
}
