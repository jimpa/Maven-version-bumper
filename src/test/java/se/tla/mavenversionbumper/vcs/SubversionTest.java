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

package se.tla.mavenversionbumper.vcs;

import static junit.framework.Assert.*;

import org.apache.commons.exec.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.tla.mavenversionbumper.Module;
import se.tla.mavenversionbumper.ReadonlyModule;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Test of the Subversion class.
 */
public class SubversionTest {

    public static final String COMMANDPATH = "Subversion.exe";
    Properties defaultCommandProperties;
    Subversion defaultSubject;
    FakeExecutor defaultExecutor;
    File pomFile;
    static final String COMMIT_MSG = "COMMITED AS ....";
    static final String LABEL = "TAG, Tag, tag";
    private static final String TAGSBASE = "tags";
    private static final String REPOSITORY_ROOT = "file://tmp/svntesting";
    private static final String INFO_RESULT =
            "Path: pom.xml\n" +
            "Name: pom.xml\n" +
            "URL: file:///tmp/svntest/trunk/pom.xml\n" +
            "Repository Root: " + REPOSITORY_ROOT + "\n" +
            "Repository UUID: 1c498fd7-26d7-4dc2-9520-2de6fb064586\n" +
            "Revision: 20\n" +
            "Node Kind: file\n" +
            "Schedule: normal\n" +
            "Last Changed Author: jimpa\n" +
            "Last Changed Rev: 15\n" +
            "Last Changed Date: 2012-05-15 11:49:23 +0200 (Tue, 15 May 2012)\n" +
            "Text Last Updated: 2012-05-15 11:49:23 +0200 (Tue, 15 May 2012)\n" +
            "Checksum: 3a504ade5daeffe91b5b56958c127779\n";

    @Before
    public void before() throws IOException {
        defaultCommandProperties = new Properties();
        defaultCommandProperties.setProperty(AbstractVersionControl.VERSIONCONTROL, Subversion.ACRONYM);
        defaultCommandProperties.setProperty(Subversion.COMMANDPATH, COMMANDPATH);
        defaultCommandProperties.setProperty(Subversion.TAGSBASE, TAGSBASE);

        defaultExecutor = new FakeExecutor();
        defaultSubject = new Subversion(defaultCommandProperties);
        defaultSubject.setExecutor(defaultExecutor);

        pomFile = File.createTempFile("foo", "bar");
        pomFile.deleteOnExit();
    }

    @After
    public void after() {
        pomFile.delete();
    }

    @Test
    public void testRestore() throws IOException {
        Module module = new FakeModule(pomFile, "foo", "bar", "1");

        defaultSubject.restore(Arrays.asList(module));

        assertEquals(1, defaultExecutor.commandLines.size());
        CommandLine commandLine = defaultExecutor.commandLines.get(0);
        assertEquals(COMMANDPATH, commandLine.getExecutable());

        String[] arguments = commandLine.getArguments();
        assertEquals(2, arguments.length);
        assertEquals("revert", arguments[0]);
        assertEquals(pomFile.getAbsolutePath(), arguments[1]);
    }

    @Test
    public void testCommit() {
        Module module = new FakeModule(pomFile, "foo", "bar", "1");

        defaultSubject.commit(Arrays.asList(module));

        assertEquals(1, defaultExecutor.commandLines.size());
        CommandLine commandLine = defaultExecutor.commandLines.get(0);
        assertEquals(COMMANDPATH, commandLine.getExecutable());

        String[] arguments = commandLine.getArguments();
        assertEquals(4, arguments.length);
        assertEquals("commit", arguments[0]);
        assertEquals("-m", arguments[1]);
        assertEquals("\"" + COMMIT_MSG + "\"", arguments[2]);
        assertEquals(pomFile.getAbsolutePath(), arguments[3]);
    }

    @Test
    public void testLabel() {
        Module module = new FakeModule(pomFile, "foo", "bar", "1");

        defaultExecutor.resultStreamAsString = INFO_RESULT;

        defaultSubject.label(Arrays.asList(module));

        // Two commands executed
        assertEquals(2, defaultExecutor.commandLines.size());
        // Command one: info to get the repository URL.

        // Command two: make the actual label.
        CommandLine commandLine = defaultExecutor.commandLines.get(1);
        assertEquals(COMMANDPATH, commandLine.getExecutable());

        String[] arguments = commandLine.getArguments();
        assertEquals(5, arguments.length);
        assertEquals("copy", arguments[0]);
        assertEquals("-m", arguments[1]);
        assertEquals("", arguments[2]);
        assertEquals(pomFile.getParentFile().getAbsolutePath(), arguments[3]);
        assertEquals("\"" + REPOSITORY_ROOT + "/" + TAGSBASE + "/" + LABEL + "\"", arguments[4]);
    }

    class FakeModule extends ReadonlyModule {
        File pomFile;

        public FakeModule(File pomFile, String group, String artifact, String version) {
            super(group, artifact, version);
            this.pomFile = pomFile;
        }

        @Override
        public String commitMessage() {
            return COMMIT_MSG;
        }

        @Override
        public String label() {
            return LABEL;
        }

        @Override
        public File pomFile() {
            return pomFile;
        }
    }
}
