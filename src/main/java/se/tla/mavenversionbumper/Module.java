package se.tla.mavenversionbumper;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import se.tla.mavenversionbumper.vcs.VersionControl;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class Module {
    final private Document document;
    final private File pomFile;
    final private Element root;
    final private Namespace nameSpace;
    private final String moduleName;
    private final VersionControl versionControl;
    private String label;

    public Module(String baseDirName, String moduleName, VersionControl versionControl) throws JDOMException, IOException {
        this.moduleName = moduleName;
        this.versionControl = versionControl;
        File dir = openDir(null, baseDirName);
        if (moduleName.length() > 0) {
            dir = openDir(dir, moduleName);
        }
        pomFile = new File(dir, "pom.xml");
        SAXBuilder builder = new SAXBuilder();
        document = builder.build(pomFile);
        root = document.getRootElement();
        nameSpace = root.getNamespace();
    }

    private File openDir(File base, String name) {
        File dir;
        if (base != null) {
             dir = new File(base, name);
        } else {
            dir = new File(name);
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("No such directory: " + dir.getName());
        }
        return dir;
    }

    public String version() {
        return myOrParent("version");
    }

    public void version(String version) {
        root.getChild("version", nameSpace).setText(version);
    }

    public String gav() {
        return groupId() + ":" + artifactId() + ":" + version();
    }

    public String groupId() {
        return myOrParent("groupId");
    }

    public void groupId(String groupId) {
        root.getChild("groupId", nameSpace).setText(groupId);
    }

    public String artifactId() {
        return root.getChildText("artifactId", nameSpace);
    }

    public void artifactId(String artifactId) {
        root.getChild("artifactId", nameSpace).setText(artifactId);
    }

    public String parentVersion() {
        return root.getChild("parent", nameSpace).getChildText("version", nameSpace);
    }

    public void parentVersion(String parentVersion) {
        root.getChild("parent", nameSpace).getChild("version", nameSpace).setText(parentVersion);
    }

    public void parentVersion(Module parent) {
        parentVersion(parent.version());
    }

    private String myOrParent(String itemName) {
        String item = root.getChildText(itemName, nameSpace);
        if (item != null) {
            return item;
        }
        return root.getChild("parent", nameSpace).getChildText(itemName);
    }

    public void updateDependency(Module moduleToUpdate) {

        // Look in dependencyManagement
        Element dep = findDependencyElement(moduleToUpdate, "dependencyManagement", "dependencies");

        if (dep == null) {
            // Look i dependencies
            dep = findDependencyElement(moduleToUpdate, "dependencies");
        }

        if (dep == null) {
            throw new IllegalArgumentException("No such dependency found");
        }

        Element version = dep.getChild("version", nameSpace);
        if (version == null) {
            throw new IllegalStateException("In " + gav() + ", no version defined for " + moduleToUpdate.gav() +
            ". Probably defined elsewhere in a dependencyManagement.");
        }

        if (version.getText().startsWith("${") && version.getText().endsWith("}")) {
            throw new IllegalStateException("In " + gav() + ", the dependency to " + moduleToUpdate.gav() +
                    "'s version is controlled by a property. Use updateProperty instead.");
        }

        version.setText(moduleToUpdate.version());
    }

    public void updatePluginDependency(Module pluginModuleToUpdate) {

        // Look in pluginManagement
        Element dep = findDependencyElement(pluginModuleToUpdate, "build", "pluginManagement", "plugins");

        if (dep == null) {
            // Look i plugins
            dep = findDependencyElement(pluginModuleToUpdate, "build", "plugins");
        }

        if (dep == null) {
            throw new IllegalArgumentException("No such plugin dependency found");
        }

        Element version = dep.getChild("version", nameSpace);
        if (version == null) {
            throw new IllegalStateException("In " + gav() + ", no version defined for " + pluginModuleToUpdate.gav() +
                    ". Probably defined elsewhere in a pluginManagement.");
        }

        if (version.getText().startsWith("${") && version.getText().endsWith("}")) {
            throw new IllegalStateException("In " + gav() + ", the plugin dependency to " + pluginModuleToUpdate.gav() +
                    "'s version is controlled by a property. Use updateProperty instead.");
        }

        version.setText(pluginModuleToUpdate.version());
    }

    public void updateProperty(String propertyName, String value) {
        Element properties = root.getChild("properties", nameSpace);
        if (properties == null) {
            throw new IllegalArgumentException("No properties defined in module " + gav());
        }

        Element property = properties.getChild(propertyName, nameSpace);
        if (property == null) {
            throw new IllegalArgumentException("No property " + property + " defined in module " + gav());
        }

        property.setText(value);
    }

    public void save() throws IOException {
        if (versionControl != null) {
            versionControl.prepareSave(pomFile.getCanonicalPath());
        }
        XMLOutputter o = new XMLOutputter();
        o.getFormat().setLineSeparator("\n"); // Nicht funktioniren
        FileUtils.write(pomFile, o.outputString(document), "utf-8");

        if (versionControl != null) {
            versionControl.checkin(pomFile.getCanonicalPath());
        }

        if (versionControl != null) {
            versionControl.label(label, pomFile.getParentFile().getCanonicalPath());
        }
    }

//    public void checkin() throws IOException {
//        if (versionControl != null) {
//            versionControl.checkin(pomFile.getCanonicalPath());
//        }
//    }

    public void label(String label) throws IOException {
        this.label = label;
    }

    private Element findDependencyElement(Module moduleToFind, String ... path) {
        Element cur = root;

        for (String pathPart : path) {
            cur = cur.getChild(pathPart, nameSpace);
            if (cur == null) {
                return null;
            }
        }

        for (Element dep : (List<Element>) cur.getChildren()) {
            String groupId = dep.getChildText("groupId", nameSpace);
            String artifactId = dep.getChildText("artifactId", nameSpace);

            if (groupId.equals(moduleToFind.groupId()) && artifactId.equals(moduleToFind.artifactId())) {
                return dep;
            }
        }

        return null;
    }

    public String toString() {
        return moduleName;
    }
}
