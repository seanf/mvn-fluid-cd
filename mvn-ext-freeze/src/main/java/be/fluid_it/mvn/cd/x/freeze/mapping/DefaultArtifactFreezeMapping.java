package be.fluid_it.mvn.cd.x.freeze.mapping;

import be.fluid_it.mvn.cd.x.freeze.FreezeExtension;
import be.fluid_it.mvn.cd.x.freeze.model.GroupIdArtifactIdVersion;
import be.fluid_it.mvn.cd.x.freeze.model.GroupIdArtifactIdVersionPrefix;
import be.fluid_it.mvn.cd.x.freeze.model.KnownElementNames;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component(role = ArtifactFreezeMapping.class)
public class DefaultArtifactFreezeMapping implements ArtifactFreezeMapping {
    private final Map<GroupIdArtifactIdVersionPrefix, GroupIdArtifactIdVersion> mapping = new HashMap<GroupIdArtifactIdVersionPrefix, GroupIdArtifactIdVersion>();
    private boolean artifactInheritsVersionFromParent = false;
    private String artifactFrozenVersion;

    @Requirement
    private Logger logger;

    // PLexus
    public DefaultArtifactFreezeMapping() {
    }
    // Testing
    DefaultArtifactFreezeMapping(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean contains(GroupIdArtifactIdVersionPrefix groupIdArtifactIdVersionPrefix) {
        return mapping.containsKey(groupIdArtifactIdVersionPrefix);
    }

    @Override
    public GroupIdArtifactIdVersion getFrozenArtifact(GroupIdArtifactIdVersionPrefix groupIdArtifactIdVersionPrefix) {
        return mapping.get(groupIdArtifactIdVersionPrefix);
    }

    @Override
    public void put(GroupIdArtifactIdVersionPrefix groupIdArtifactIdVersionPrefix, GroupIdArtifactIdVersion groupIdArtifactIdVersion) {
        mapping.put(groupIdArtifactIdVersionPrefix, groupIdArtifactIdVersion);
        logger.debug("[ArtifactFreezeMapping]: Add " +
                groupIdArtifactIdVersionPrefix +
                " -> " +
                groupIdArtifactIdVersion);
    }

    public void put(String revision, InputStream pomStream) {
        GroupIdArtifactIdVersionPrefix pomGroupIdArtifactIdVersionPrefix = null;
        pomGroupIdArtifactIdVersionPrefix = extractGroupIdArtifactIdVersionPrefix(pomStream);
        GroupIdArtifactIdVersion frozenPomGroupIdArtifactIdVersion = pomGroupIdArtifactIdVersionPrefix.addRevision(revision);
        put(pomGroupIdArtifactIdVersionPrefix, frozenPomGroupIdArtifactIdVersion);
        logger.info("[ArtifactFreezeMapping]: Freeze the pom as artifact " + frozenPomGroupIdArtifactIdVersion);

    }

    @Override
    public void put(String revision, File pom) {
        try {
            put(revision, new FileInputStream(pom));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private GroupIdArtifactIdVersionPrefix extractGroupIdArtifactIdVersionPrefix(InputStream pomStream) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader reader =
                    factory.createXMLStreamReader(pomStream);
            ElementTree tree = new ElementTree(KnownElementNames.PROJECT);
            tree.root().addChild(KnownElementNames.MODEL_VERSION);
            tree.root().addChild(KnownElementNames.PARENT).addChild(new String[] {KnownElementNames.GROUP_ID, KnownElementNames.ARTIFACT_ID, KnownElementNames.VERSION, KnownElementNames.RELATIVE_PATH});
            tree.root().addChild(KnownElementNames.GROUP_ID, KnownElementNames.ARTIFACT_ID, KnownElementNames.VERSION, KnownElementNames.PACKAGING, KnownElementNames.NAME, KnownElementNames.DESCRIPTION);

            boolean elementFound = true;
            while (elementFound && reader.hasNext()) {
                int event = reader.next();

                switch(event){
                    case XMLStreamConstants.START_ELEMENT:
                        elementFound = tree.moveToChild(reader.getLocalName());
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        String trimmedText = reader.getText().trim();
                        if (!"".equals(trimmedText)) {
                            tree.handle(trimmedText);
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        tree.moveToParent();
                        break;
                    default:
                        break;
                }
            }
            artifactInheritsVersionFromParent = !tree.artifactOverridesVersionFromParent();
            GroupIdArtifactIdVersionPrefix groupIdArtifactIdVersionPrefix = tree.groupIdArtifactIdVersion().stripSnapshotPostfix();
            artifactFrozenVersion = groupIdArtifactIdVersionPrefix.versionPrefix() + "-" + FreezeExtension.getRevision();
            return groupIdArtifactIdVersionPrefix;
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean artifactInheritsVersionOfParent() {
        return artifactInheritsVersionFromParent;
    }

    @Override
    public String artifactFrozenVersion() {
        return artifactFrozenVersion;
    }

}