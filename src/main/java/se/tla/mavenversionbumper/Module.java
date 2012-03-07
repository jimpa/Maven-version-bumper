/*
 * Copyright (c) 2012 Jim Svensson <jimpa@tla.se>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package se.tla.mavenversionbumper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * Represents a Maven project file, pom.xml.
 *
 * It contains methods to easily view and manipulate dependency information.
 */
public class Module {
    final private Document document;
    final private File pomFile;
    final private Element root;
    final private Namespace nameSpace;
    final private String moduleName;
    final private String originalVersion;
    private String label;
    private String commitMessage;
    private boolean labelOnlyPomXml = false;

    /**
     * Constructor.
     *
     * @param baseDirName Filename of the base directory of the Maven module.
     * @param moduleName The symbolic name of the Maven module.
     * @throws JDOMException Problem reading the pom.xml file.
     * @throws IOException Problem reading the pom.xml file.
     */
    public Module(String baseDirName, String moduleName) throws JDOMException, IOException {
        this.moduleName = moduleName;
        File dir = openDir(null, baseDirName);
        if (moduleName.length() > 0) {
            dir = openDir(dir, moduleName);
        }
        File baseDir = dir;
        pomFile = new File(baseDir, "pom.xml");
        SAXBuilder builder = new SAXBuilder();
        document = builder.build(pomFile);
        root = document.getRootElement();
        nameSpace = root.getNamespace();
        originalVersion = version();
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

    /**
     * @return GAV-coordinates. GroupId, ArtifactId, Version.
     */
    public String gav() {
        return groupId() + ":" + artifactId() + ":" + version();
    }

    public String groupId() {
        return myOrParent("groupId");
    }

    /**
     * @param groupId New GroupId.
     */
    public void groupId(String groupId) {
        root.getChild("groupId", nameSpace).setText(groupId);
    }

    public String artifactId() {
        return root.getChildText("artifactId", nameSpace);
    }

    /**
     * @param artifactId New ArtifactId.
     */
    public void artifactId(String artifactId) {
        root.getChild("artifactId", nameSpace).setText(artifactId);
    }

    public String version() {
        return myOrParent("version");
    }

    /**
     * @param version New Version.
     */
    public void version(String version) {
        if (commitMessage == null) {
            commitMessage = "Bump " + originalVersion + " -> " + version;
        }
        root.getChild("version", nameSpace).setText(version);
    }

    public String parentVersion() {
        Element parent = root.getChild("parent", nameSpace);
        if (parent == null) {
            return null;
        }
        return parent.getChildText("version", nameSpace);
    }

    /**
     * @param parentVersion New parentVersion.
     */
    public void parentVersion(String parentVersion) {
        Element parent = root.getChild("parent", nameSpace);
        if (parent == null) {
            throw new IllegalArgumentException("No parent defined in module");
        }
        Element version = parent.getChild("version", nameSpace);
        if (version == null) {
            throw new IllegalStateException("No version defined for parent.");
        }
        version.setText(parentVersion);
    }

    /**
     * Update the parent version to that of this Module.
     *
     * @param parent Use this modules version.
     */
    public void parentVersion(Module parent) {
        parentVersion(parent.version());
    }

    private String myOrParent(String itemName) {
        String item = root.getChildText(itemName, nameSpace);
        if (item != null) {
            return item;
        }
        Element parent = root.getChild("parent", nameSpace);
        if (parent == null) {
            return null;
        }
        return parent.getChildText(itemName, nameSpace);
    }

    /**
     * Find this module in either the modules dependency management list or in the dependency list.
     *
     * @param moduleToUpdate Module to find and update version for.
     * @throws IllegalArgumentException If the moduleToUpdate can't be found in either list.
     */
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
            throw new IllegalArgumentException("In " + gav() + ", no version defined for " + moduleToUpdate.gav() +
            ". Probably defined elsewhere in a dependencyManagement.");
        }

        if (version.getText().startsWith("${") && version.getText().endsWith("}")) {
            throw new IllegalArgumentException("In " + gav() + ", the dependency to " + moduleToUpdate.gav() +
                    "'s version is controlled by a property. Use updateProperty instead.");
        }

        version.setText(moduleToUpdate.version());
    }

    /**
     * Find this plugin in either the modules plugin management list or in the plugin list.
     *
     * @param pluginToUpdate Plugin to find and update version for.
     * @throws IllegalArgumentException If the moduleToUpdate can't be found in either list.
     */
    public void updatePluginDependency(Module pluginToUpdate) {

        // Look in pluginManagement
        Element dep = findDependencyElement(pluginToUpdate, "build", "pluginManagement", "plugins");

        if (dep == null) {
            // Look i plugins
            dep = findDependencyElement(pluginToUpdate, "build", "plugins");
        }

        if (dep == null) {
            throw new IllegalArgumentException("No such plugin dependency found");
        }

        Element version = dep.getChild("version", nameSpace);
        if (version == null) {
            throw new IllegalArgumentException("In " + gav() + ", no version defined for " + pluginToUpdate.gav() +
                    ". Probably defined elsewhere in a pluginManagement.");
        }

        if (version.getText().startsWith("${") && version.getText().endsWith("}")) {
            throw new IllegalArgumentException("In " + gav() + ", the plugin dependency to " + pluginToUpdate.gav() +
                    "'s version is controlled by a property. Use updateProperty instead.");
        }

        version.setText(pluginToUpdate.version());
    }

    /**
     * Find the named property and update its value.
     *
     * @param propertyName Name.
     * @param value Value.
     * @throws IllegalArgumentException if named property can't be found.
     */
    public void updateProperty(String propertyName, String value) {
        Element properties = root.getChild("properties", nameSpace);
        if (properties == null) {
            throw new IllegalArgumentException("No properties defined in module " + gav());
        }

        Element property = properties.getChild(propertyName, nameSpace);
        if (property == null) {
            throw new IllegalArgumentException("No property " + propertyName + " defined in module " + gav());
        }

        property.setText(value);
    }

    /**
     * Save this module back to its original pom.xml file.
     *
     * If a VersionControl was provided while creating this Module, the pom.xml is first
     * commited and then, optionally, labeled.
     *
     * @throws IOException in case of IO-related problems.
     */
    public void save() throws IOException {
        XMLOutputter o = new XMLOutputter();
        // TODO Make sure that the line endings are preserved.
        o.getFormat().setLineSeparator("\n"); // Nicht funktioniren
        // TODO Make sure that the character encoding of the pom.xml is preserved.
        FileUtils.write(pomFile, o.outputString(document), "utf-8");
    }

    /**
     * Apply this label to the Module when it is saved. Requires that a VersionControl was provided to work.
     *
     * @param label Label.
     */
    public void label(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /**
     * Use this commit message if a commit to VersionControl is performed. If no custom message is provided,
     * a default message is used.
     *
     * @param commitMessage Custom message to use during a commit.
     */
    public void commitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    /**
     * @return The message to use during a commit.
     */
    public String commitMessage() {
        return commitMessage;
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

    /**
     * Controls how much will be labeled if labeling is required.
     *
     * Please observe that this functionality isn't implemented in all VersionControl implementations since
     * they simply don't support labeling of sub trees.
     *
     * @param labelOnlyPomXml If false (default) label whole Module recursively, if true only label the pom.xml.
     */
    public void labelOnlyPomXml(boolean labelOnlyPomXml) {
        this.labelOnlyPomXml = labelOnlyPomXml;
    }

    /**
     * @return If true, only label the pom.xml, if false, label whole file tree recursively, including any sub module..
     */
    public boolean labelOnlyPomXml() {
        return labelOnlyPomXml;
    }

    /**
     * @return The modules pom.xml file.
     */
    public File pomFile() {
        return pomFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return moduleName;
    }
}
