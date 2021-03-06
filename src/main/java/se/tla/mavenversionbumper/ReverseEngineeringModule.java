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

import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ReverseEngineeringModule extends Module implements Comparable<ReverseEngineeringModule> {

    private ReverseEngineeringModule parent;
    private List<ReverseEngineeringModule> dependencies;
    private List<ReverseEngineeringModule> pluginDependencies;

    public ReverseEngineeringModule(File baseDir, String modulePath) throws JDOMException, IOException {
        super(baseDir.getAbsolutePath(), modulePath);
    }

    public List<String> subModules() {
        List<String> result = new ArrayList<String>();
        Element modulesElement = root.getChild("modules", nameSpace);
        if (modulesElement != null) {
            List<Element> modules = (List<Element>) modulesElement.getChildren();
            for (Element module : modules) {
                result.add(module.getText());
            }
        }

        return result;
    }

    public String moduleName() {
        return artifactId().replaceAll("[-]", "");
    }

    public String path() {
        return this.moduleName;
    }

    public ReverseEngineeringModule detectParent(List<ReverseEngineeringModule> modules) {

        for (ReverseEngineeringModule module : modules) {
            try {
                parentVersion(module);
                return module;
            } catch (IllegalArgumentException e) {
                // Expected when this isn't a dependency. Continue looking.
            }
        }
        return null;
    }

    public List<ReverseEngineeringModule> findDependencies(List<ReverseEngineeringModule> possibleModules) {
        List<ReverseEngineeringModule> result = new ArrayList<ReverseEngineeringModule>();

        for (ReverseEngineeringModule module : possibleModules) {
            try {
                updateDependency(module);
                result.add(module);
            } catch (IllegalArgumentException e) {
                // Expected when this isn't a dependency. Ignored.
            }
        }

        return result;
    }

    public List<ReverseEngineeringModule> findPluginDependencies(List<ReverseEngineeringModule> possiblePluginModules) {
        List<ReverseEngineeringModule> result = new ArrayList<ReverseEngineeringModule>();

        for (ReverseEngineeringModule module : possiblePluginModules) {
            try {
                updatePluginDependency(module);
                result.add(module);
            } catch (IllegalArgumentException e) {
                // Expected when this isn't a plugin dependency. Ignored.
            }
        }

        return result;
    }

    public void consider(List<ReverseEngineeringModule> modules) {
        parent = detectParent(modules);
        dependencies = findDependencies(modules);
        pluginDependencies = findPluginDependencies(modules);
    }

    public String getLoadStatement() {
        if (version() != null) {
            return moduleName() + " = load(\"" + path() + "\", \"" + version() + "\");\n";
        } else {
            return moduleName() + " = load(\"" + path() + "\");\n";
        }
    }

    public String getDependencyStatements() {
        StringBuilder builder = new StringBuilder();
        if (parent != null) {
            builder.append(moduleName()).append(".parentVersion(").append(parent.moduleName()).append(");\n");
        }
        for (ReverseEngineeringModule dependency : dependencies) {
            builder.append(moduleName()).append(".updateDependency(").append(dependency.moduleName()).append(");\n");
        }
        for (ReverseEngineeringModule dependency : pluginDependencies) {
            builder.append(moduleName()).append(".updatePluginDependency(").append(dependency.moduleName()).append(");\n");
        }
        builder.append("\n");

        return builder.toString();
    }

    /**
     * @return The original version of this module, or null if none was defined in the pom.xml.
     */
    @Override
    public String version() {
        return originalVersion;
    }

    @Override
    public int compareTo(ReverseEngineeringModule compareToThis) {
        return moduleName().compareTo(compareToThis.moduleName());
    }
}
