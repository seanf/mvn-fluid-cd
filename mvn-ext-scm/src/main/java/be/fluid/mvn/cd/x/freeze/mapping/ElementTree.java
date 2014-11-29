package be.fluid.mvn.cd.x.freeze.mapping;

import be.fluid.mvn.cd.x.freeze.model.GroupIdArtifactIdVersion;

import java.util.LinkedList;
import java.util.List;

import static be.fluid.mvn.cd.x.freeze.model.KnownElementNames.*;

public class ElementTree {
    private Element root;
    private Element current;
    private int currentDepth = 0;
    private GroupIdArtifactIdVersion groupIdArtifactIdVersion = new GroupIdArtifactIdVersion();
    private boolean artifactOverridesVersionFromParent = false;
    private boolean inParentElement = false;

    public ElementTree(String rootName) {
        root = new Element(rootName);
    }

    public Element root() {
        return root;
    }

    public Element current() {
        return current;
    }

    public boolean moveToChild(String name) {
        if (name != null) {
            if (name.equals(PARENT)) {
                inParentElement = true;
            }
            if (current != null) {
                for (Element child : current.children) {
                    if (name.equals(child.name)) {
                        this.current = child;
                        return true;
                    }
                }
            } else {
                if (name.equals(root.name)) {
                    this.current = root;
                    return true;
                }
            }
        }
        return false;
    }

    public void moveToParent() {
        if (PARENT.equals(this.current.name)) {
            inParentElement = false;
        }
        this.current = this.current.parent;
    }

    public void handle(String value) {
        switch (current.name) {
            case "groupId":
                groupIdArtifactIdVersion = groupIdArtifactIdVersion.addGroupId(value);
                break;
            case "artifactId":
                groupIdArtifactIdVersion = groupIdArtifactIdVersion.addArtifactId(value);
                break;
            case "version":
                if (!inParentElement) {
                    artifactOverridesVersionFromParent = true;
                }
                groupIdArtifactIdVersion = groupIdArtifactIdVersion.addVersion(value);
                break;
            default:
                break;
        }
    }

    public GroupIdArtifactIdVersion groupIdArtifactIdVersion() {
        return groupIdArtifactIdVersion;
    }

    public boolean artifactOverridesVersionFromParent() {
        return  artifactOverridesVersionFromParent;
    }

    public static class Element {
        private String name;
        private Element parent;
        private final List<Element> children = new LinkedList<Element>();

        public Element(String name) {
            this.name = name;
        }

        public Element addChild(String... names) {
            Element child = null;
            for (String name : names) {
                child = new Element(name);
                child.parent = this;
                children.add(child);

            }
            return child;
        }
    }
}