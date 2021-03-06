package be.fluid_it.mvn.cd.x.freeze.stamp.revision;

import be.fluid_it.mvn.cd.x.freeze.stamp.Stamper;
import be.fluid_it.mvn.cd.x.freeze.stamp.TemplateStamper;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import java.util.Properties;

@Component( role = Stamper.class, hint = RevisionStamper.HINT)
public class RevisionStamper extends TemplateStamper<RevisionStamp> {
    public static final String REVISION = "revision";
    public final static String HINT = REVISION;
    public static final String REVISION_TEMPLATE = "${revision}";

    @Requirement
    private Logger logger;

    public RevisionStamper() {
        super(REVISION_TEMPLATE);
    }

    @Override
    public RevisionStamp createStamp(Properties props) {
        return new RevisionStamp(props.getProperty(REVISION));
    }


    @Override
    public Logger logger() {
        return logger;
    }
}
