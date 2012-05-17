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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the Module class.
 */
public class ModuleTest {

    @Test
    public void testSimple() throws Exception {
        Module subject = new Module("target/test-classes/sources", "simple");

        assertEquals("se.tla.maven", subject.groupId());
        assertEquals("versionbumper", subject.artifactId());
        assertEquals("1.0-SNAPSHOT", subject.version());
        assertEquals("se.tla.maven:versionbumper:1.0-SNAPSHOT", subject.gav());
    }

    @Test
    public void testParentVersion() throws Exception {
        Module subject = new Module("target/test-classes", "withparent");

        assertEquals("0.1-SNAPSHOT", subject.parentVersion());
    }

    @Test
    public void testItemsFromParent() throws Exception {
        Module subject = new Module("target/test-classes", "withparent");

        assertEquals("se.tla.maven", subject.groupId());
        assertEquals("0.1-SNAPSHOT", subject.version());
    }

    @Test
    public void testParentVersionWithNoParent() throws Exception {
        Module subject = new Module("target/test-classes", "simple");

        assertNull(subject.parentVersion());

        try {
            subject.parentVersion("1.0");
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            Module parent = new Module("target/test-classes", "simple");
            subject.parentVersion(parent);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testParentMissingVersion() throws Exception {
        Module subject = new Module("target/test-classes/withparent", "missingversion");

        try {
            subject.parentVersion("1.0");
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test
    public void testParentUpdateWithWrongModule() throws Exception {
        Module subject = new Module("target/test-classes/sources", "withparent");
        Module parent = new Module("target/test-classes/sources", "withproperty");

        try {
            subject.parentVersion(parent);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testSetParentVersion() throws Exception {
        ModuleTestTemplate.template("withparent", "withparent.xml", new ModuleTinker() {
            @Override
            public void tink(Module subject) {
                subject.parentVersion("4711");
            }
        });
    }

    @Test
    public void testUpdateDependency() throws Exception {
        final Module depmod = new Module("target/test-classes", "simple");
        ModuleTestTemplate.template("dependency", "dependency.xml", new ModuleTinker() {
            @Override
            public void tink(Module subject) {
                subject.updateDependency(depmod);
            }
        });
    }

    @Test
    public void testUpdateDependencyManagement() throws Exception {
        final Module depmod = new Module("target/test-classes", "simple");
        ModuleTestTemplate.template("dependencyManagement", "dependencyManagement.xml", new ModuleTinker() {
            @Override
            public void tink(Module subject) {
                subject.updateDependency(depmod);
            }
        });
    }

    @Test
    public void testUpdatePluginDependency() throws Exception {
        final Module depmod = new Module("target/test-classes", "simple");
        ModuleTestTemplate.template("pluginDependency", "pluginDependency.xml", new ModuleTinker() {
            @Override
            public void tink(Module subject) {
                subject.updatePluginDependency(depmod);
            }
        });
    }

    @Test
    public void testUpdatePluginDependencyManagement() throws Exception {
        final Module depmod = new Module("target/test-classes", "simple");
        ModuleTestTemplate.template("pluginManagement", "pluginManagement.xml", new ModuleTinker() {
            @Override
            public void tink(Module subject) {
                subject.updatePluginDependency(depmod);
            }
        });
    }

    @Test
    public void testUpdateProperty() throws Exception {
        ModuleTestTemplate.template("withproperty", "withproperty.xml", new ModuleTinker() {
            @Override
            public void tink(Module subject) {
                subject.updateProperty("coffee", "dark");
            }
        });
    }

    @Test
    public void testUpdateUnknownProperty() throws Exception {
        try {
            ModuleTestTemplate.template("withproperty", "withproperty.xml", new ModuleTinker() {
                @Override
                public void tink(Module subject) {
                    subject.updateProperty("tea", "lipton");
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testUpdateDependencyAsProperty() throws Exception {
        final Module depmod = new Module("target/test-classes", "simple");
        ModuleTestTemplate.template("dependencyAsProperty", "dependencyAsProperty.xml", new ModuleTinker() {
            @Override
            public void tink(Module subject) {
                subject.updateDependency(depmod);
            }
        });
    }

    @Test
    public void testUpdatePluginAsProperty() throws Exception {
        final Module depmod = new Module("target/test-classes", "simple");
        ModuleTestTemplate.template("pluginAsProperty", "pluginAsProperty.xml", new ModuleTinker() {
            @Override
            public void tink(Module subject) {
                subject.updatePluginDependency(depmod);
            }
        });
    }

    @Test
    public void testUpdatePropertyAsMissingProperty() throws Exception {
        try {
            final Module depmod = new Module("target/test-classes", "simple");
            ModuleTestTemplate.template("dependencyAsMissingProperty", "dependencyAsProperty.xml", new ModuleTinker() {
                @Override
                public void tink(Module subject) {
                    subject.updateDependency(depmod);
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testFindAllSnapshots() throws Exception {
        final Module subject = new Module("target/test-classes/sources", "withAllSnapshots");

        List<String> result = subject.findSnapshots();

        assertEquals(7, result.size());
        assertEquals("Module version", result.get(0));
        assertEquals("Parent version 2-SNAPSHOT", result.get(1));
        assertEquals("Property testVersion:3-SNAPSHOT", result.get(2));
        assertEquals("Dependency foo:dependency:4-SNAPSHOT", result.get(3));
        assertEquals("Dependency management foo:dependencymanagement:5-SNAPSHOT", result.get(4));
        assertEquals("Plugin foo:plugin:6-SNAPSHOT", result.get(5));
        assertEquals("Plugin management foo:pluginmanagement:7-SNAPSHOT", result.get(6));
    }

    @Test
    public void testFindNoSnapshots() throws Exception {
        final Module subject = new Module("target/test-classes/sources", "withNoSnapshots");

        List<String> result = subject.findSnapshots();

        assertEquals(0, result.size());
    }
}
