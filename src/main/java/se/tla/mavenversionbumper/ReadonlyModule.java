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

import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Extension of the Module class that only supplies the GAV coordinates. All other methods
 * fails. Used to express Maven modules that we have no control over, but need to handle
 * dependency towards.
 */
public class ReadonlyModule extends Module {
    private final String groupId;
    private final String artifactId;
    private final String version;

    public ReadonlyModule(@SuppressWarnings("UnusedParameters") String baseDirName, @SuppressWarnings("UnusedParameters") String moduleName) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    public ReadonlyModule(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public String gav() {
        return groupId() + ":" + artifactId() + ":" + version();
    }

    @Override
    public String groupId() {
        return groupId;
    }

    @Override
    public void groupId(String groupId) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public String artifactId() {
        return artifactId;
    }

    @Override
    public void artifactId(String artifactId) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public void version(String version) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public String parentVersion() {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public void parentVersion(String parentVersion) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public void parentVersion(Module newParent) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public void updateDependency(Module moduleToUpdate) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public void updatePluginDependency(Module pluginToUpdate) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public void updateProperty(String propertyName, String value) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public void save() throws IOException {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public void label(String label) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public String label() {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public void commitMessage(String commitMessage) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public String commitMessage() {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public List<String> findSnapshots() {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public void labelOnlyPomXml(boolean labelOnlyPomXml) {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public boolean labelOnlyPomXml() {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public File pomFile() {
        throw new UnsupportedOperationException("No supported in readonly modules");
    }

    @Override
    public String toString() {
        return gav();
    }
}
