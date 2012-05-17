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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Test of the Git class.
 */
public class GitTest {

    public static final String COMMANDPATH = "git.exe";
    Properties defaultCommandProperties;
    Git defaultSubject;
    FakeExecutor defaultExecutor;
    File pomFile;
    static final String COMMIT_MSG = "COMMITED AS ....";
    static final String LABEL = "TAG, Tag, tag";

    @Before
    public void before() throws IOException {
        defaultCommandProperties = new Properties();
        defaultCommandProperties.setProperty(AbstractVersionControl.VERSIONCONTROL, Subversion.ACRONYM);
        defaultCommandProperties.setProperty(Git.COMMANDPATH, COMMANDPATH);

        defaultExecutor = new FakeExecutor();
        defaultSubject = new Git(defaultCommandProperties);
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
        Module module = new TestableModule(pomFile, "foo", "bar", "1", null, null);

        defaultSubject.restore(Arrays.asList(module));

        assertEquals(1, defaultExecutor.commandLines.size());
        CommandLine commandLine = defaultExecutor.commandLines.get(0);
        assertEquals(COMMANDPATH, commandLine.getExecutable());

        String[] arguments = commandLine.getArguments();
        assertEquals(2, arguments.length);
        assertEquals("checkout", arguments[0]);
        assertEquals(pomFile.getName(), arguments[1]);
    }

    @Test
    public void testCommit() {
        Module module = new TestableModule(pomFile, "foo", "bar", "1", COMMIT_MSG, null);

        defaultSubject.commit(Arrays.asList(module));

        assertEquals(1, defaultExecutor.commandLines.size());
        CommandLine commandLine = defaultExecutor.commandLines.get(0);
        assertEquals(COMMANDPATH, commandLine.getExecutable());

        String[] arguments = commandLine.getArguments();
        assertEquals(4, arguments.length);
        assertEquals("commit", arguments[0]);
        assertEquals("-m", arguments[1]);
        assertEquals("\"" + COMMIT_MSG + "\"", arguments[2]);
        assertEquals(pomFile.getName(), arguments[3]);
    }

    @Test
    public void testLabel() {
        Module module = new TestableModule(pomFile, "foo", "bar", "1", null, LABEL);

        defaultSubject.label(Arrays.asList(module));

        assertEquals(1, defaultExecutor.commandLines.size());
        CommandLine commandLine = defaultExecutor.commandLines.get(0);
        assertEquals(COMMANDPATH, commandLine.getExecutable());

        String[] arguments = commandLine.getArguments();
        assertEquals(3, arguments.length);
        assertEquals("tag", arguments[0]);
        assertEquals("-f", arguments[1]);
        assertEquals("\"" + LABEL + "\"", arguments[2]);
    }
}
