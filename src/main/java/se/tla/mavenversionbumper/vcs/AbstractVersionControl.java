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

import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;

/**
 * Common functionality for implementations of the VersionControl interface.
 */
public abstract class AbstractVersionControl implements VersionControl {

    public static final String VERSIONCONTROL = "versioncontrol";

    @Override
    public void prepareSave(File file) {
        // Default is to do nothing before saving.
    }

    /**
     * Execute this command line, optionally in this working directory.
     * @param cmdLine
     * @param workDir
     */
    protected void execute(CommandLine cmdLine, File workDir) {
        DefaultExecutor exec = new DefaultExecutor();
        exec.setWatchdog(new ExecuteWatchdog(60000));
        if (workDir != null) {
            exec.setWorkingDirectory(workDir);
        }

        System.out.println("Running command:   " + cmdLine.toString());

        try {
            exec.execute(cmdLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
