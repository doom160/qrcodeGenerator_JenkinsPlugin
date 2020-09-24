package sg.com.renfa;

import java.io.IOException;
import java.io.File;

import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import jenkins.tasks.SimpleBuildStep;
import sg.com.renfa.QRCodeGenerator;


public class QRCodeBuilder extends Builder implements SimpleBuildStep {

    private String message, title;
    private int dimension = 400;

    @DataBoundConstructor
    public QRCodeBuilder(String message, String title) {
        this.message = message;
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    FilePath getMasterLogDirectory(AbstractBuild build) throws IOException, InterruptedException {
        String buildDir = build.getRootDir().getAbsolutePath();
        FilePath masterLogDirectory = new FilePath(new File(buildDir + File.separator + "qr" + File.separator + "junitResult.xml"));
        return masterLogDirectory;
      }
      
      FilePath getSlaveWorkspace(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        String workspacePath = build.getEnvironment(listener).get("WORKSPACE");
          if (workspacePath == null) {
            throw new IOException(Messages.QRCodeBuilder_shortMessage());
          }
      
        FilePath projectWorkspaceOnSlave = new FilePath(launcher.getChannel(), workspacePath);
        projectWorkspaceOnSlave.mkdirs();
        return projectWorkspaceOnSlave.absolutize();
      }


    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        String sanitizedMessage = message.replaceAll("[^0-9a-zA-Z]+","");
        FilePath tmpPath = workspace.createTempDir("qrcode", "tmp");
        String qrWorkspacePath = String.format("%s/%s.png",tmpPath.getRemote(),sanitizedMessage);
        QRCodeGenerator.generateQRCodeImage(message, dimension, dimension, qrWorkspacePath);

        FilePath slaveQrPath = new FilePath(tmpPath, sanitizedMessage + ".png");
        String masterBuildDir = run.getRootDir().getAbsolutePath();
        
        slaveQrPath.copyTo(new FilePath(new File(masterBuildDir +  File.separator + sanitizedMessage + ".png")));

     //   FilePath qrCode = tmpPath.child("target").child("qr.png");
    //    qrCode.copyFrom(getClass().getResourceAsStream("qr.png"));
       // workspace.copyTo(new FilePath(new File("something.png")));
        //listener.getLogger().println(workspace.localChannel
     //   listener.getLogger().println("Hello, " + message + "!");
            
    }

    @Symbol("qrCode")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckMessage(@QueryParameter String message)
                throws IOException, ServletException {
            if (message.length() == 0) {
                return FormValidation.error(Messages.QRCodeBuilder_emptyMessage());
            }
            if (message.length() < 4) {
                return FormValidation.warning(Messages.QRCodeBuilder_shortMessage());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTitle(@QueryParameter String title)
                throws IOException, ServletException {
            if (title.length() == 0) {
                return FormValidation.error(Messages.QRCodeBuilder_emptyMessage());
            }
            if (title.length() < 4) {
                return FormValidation.warning(Messages.QRCodeBuilder_shortMessage());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.QRCodeBuilder_DisplayName();
        }

    }

}
