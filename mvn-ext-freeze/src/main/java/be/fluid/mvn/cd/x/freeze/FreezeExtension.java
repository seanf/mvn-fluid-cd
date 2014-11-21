package be.fluid.mvn.cd.x.freeze;

import be.fluid.mvn.cd.x.freeze.mapping.ArtifactFreezeMapping;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

@Component( role = AbstractMavenLifecycleParticipant.class, hint = FreezeExtension.FREEZE)
public class FreezeExtension extends AbstractMavenLifecycleParticipant {
    public static final String FREEZE = "freeze";

    public static final String REVISION_SYSTEM_PROPERTY = "revision";

    public static boolean freezingEnabled() {
        return System.getProperty(REVISION_SYSTEM_PROPERTY) != null;
    }

    public static String getRevision() {
        return System.getProperty(REVISION_SYSTEM_PROPERTY);
    }

    public static void setRevision(String revision) {
        System.setProperty(REVISION_SYSTEM_PROPERTY, revision);
    }

    @Requirement
    private Logger logger;

    @Requirement
    private ArtifactFreezeMapping artifactFreezeMapping;

    @Override
    public void afterSessionStart(MavenSession session)
            throws MavenExecutionException {
        if (freezingEnabled()) {
            logger.info("[FreezeExtension]: Freezing poms ...");
            artifactFreezeMapping.put(getRevision(), session.getRequest().getPom());
        }
    }

    @Override
    public void afterSessionEnd( MavenSession session )
            throws MavenExecutionException {
        if (freezingEnabled()) {
            logger.info("[FreezeExtension]: Poms are frozen ...");
        }
    }

}
