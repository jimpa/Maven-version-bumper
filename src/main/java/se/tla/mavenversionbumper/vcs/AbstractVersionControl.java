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
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import se.tla.mavenversionbumper.Module;

/**
 * Common functionality for implementations of the VersionControl interface.
 */
@SuppressWarnings("ALL")
public abstract class AbstractVersionControl implements VersionControl {

    public static final String VERSIONCONTROL = "versioncontrol";
    private static final int DEFAULTTIMEOUT = 60000;

    private Executor executor = new DefaultExecutor();

    /**
     * {@inheritDoc}
     */
    @Override
    public void before(List<Module> modules) {
        // Default is to do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void after(List<Module> modules) {
        // Default is to do nothing.
    }

    /**
     * Execute this command line, optionally in this working directory. Timeout of command is set to 60 seconds
     * @param cmdLine Command line to execute.
     * @param workDir Working directory to set before execution, or null if process default working directory should be used.
     */
    protected void execute(CommandLine cmdLine, File workDir) {
        execute(cmdLine, workDir, DEFAULTTIMEOUT);
    }

    /**
     * Execute this command line, optionally in this working directory.
     * @param cmdLine Command line to execute.
     * @param workDir Working directory to set before execution, or null if process default working directory should be used.
     * @param timeout Time out in ms. If -1, don't set any time out.
     */
    protected void execute(CommandLine cmdLine, File workDir, int timeout) {
        execute(cmdLine, workDir,  timeout, null);
    }

    /**
     * Execute this command line, optionally in this working directory.
     * @param cmdLine Command line to execute.
     * @param workDir Working directory to set before execution, or null if process default working directory should be used.
     * @param timeout Time out in ms. If -1, don't set any time out.
     * @param executeStreamHandler Special stream handler to use, or null for use of the default.
     */
    protected void execute(CommandLine cmdLine, File workDir, int timeout, ExecuteStreamHandler executeStreamHandler) {

        if (timeout != -1) {
            executor.setWatchdog(new ExecuteWatchdog(timeout));
        }
        if (workDir != null) {
            executor.setWorkingDirectory(workDir);
        }
        if (executeStreamHandler != null) {
            executor.setStreamHandler(executeStreamHandler);
        }

        System.out.println("Running command:   " + cmdLine.toString());

        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
