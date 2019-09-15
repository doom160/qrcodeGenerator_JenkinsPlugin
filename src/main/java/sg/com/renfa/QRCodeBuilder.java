package sg.com.renfa;

import java.io.IOException;

import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import sg.com.renfa.QRCodeGenerator;

public class QRCodeBuilder extends Builder implements SimpleBuildStep {

    private final String message;
    private final int dimension;

    @DataBoundConstructor
    public QRCodeBuilder(String message, int dimension) {
        this.message = message;
        this.dimension = (dimension < 200) ? 400 : dimension; 
    }

    public String getMessage() {
        return message;
    }

    public int getDimension() {
        return dimension;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        QRCodeGenerator.generateQRCodeImage(message, dimension, dimension, "something.png");
        listener.getLogger().println("Hello, " + message + "!");
            
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

        public FormValidation doCheckDimension(@QueryParameter String dimension)
                throws IOException, ServletException {
            int intDimension = 0;
            try {
                intDimension = Integer.parseInt(dimension);
            } catch(Exception ex){
                return FormValidation.error(Messages.QRCodeBuilder_notIntDimension());
            }
            if (intDimension < 200) {
                return FormValidation.warning(Messages.QRCodeBuilder_istooSmallDimension());
            }
            if (intDimension > 1000) {
                return FormValidation.warning(Messages.QRCodeBuilder_istooBigDimension());
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
