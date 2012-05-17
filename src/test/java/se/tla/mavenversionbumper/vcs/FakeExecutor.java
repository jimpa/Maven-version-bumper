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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.ProcessDestroyer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Does nothing besides saving the provided command line.
 */
public class FakeExecutor implements Executor {

    public List<CommandLine> commandLines = new LinkedList<CommandLine>();
    public String resultStreamAsString;
    public String errorStreamAsString;
    public OutputStream outputStream;
    public OutputStream errorStream;

    @Override
    public void setExitValue(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExitValues(int[] values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFailure(int exitValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExecuteStreamHandler getStreamHandler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStreamHandler(ExecuteStreamHandler streamHandler) {
        if (streamHandler instanceof ExposingPumpStreamHandler) {
            this.outputStream = ((ExposingPumpStreamHandler) streamHandler).getOutputStream();
            this.errorStream = ((ExposingPumpStreamHandler) streamHandler).getErrorStream();
        } else {
            throw new IllegalArgumentException("Sorry, can only fake streams using a ExposingPumpStreamHandler");
        }
    }

    @Override
    public ExecuteWatchdog getWatchdog() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWatchdog(ExecuteWatchdog watchDog) {
    }

    @Override
    public ProcessDestroyer getProcessDestroyer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProcessDestroyer(ProcessDestroyer processDestroyer) {
    }

    @Override
    public File getWorkingDirectory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWorkingDirectory(File dir) {
    }

    @Override
    public int execute(CommandLine command) throws ExecuteException, IOException {
        commandLines.add(command);
        if (resultStreamAsString != null) {
            outputStream.write(resultStreamAsString.getBytes("ISO-8859-1"));
        }
        if (errorStreamAsString != null) {
            errorStream.write(errorStreamAsString.getBytes("ISO-8859-1"));
        }
        return 0;
    }

    @Override
    public int execute(CommandLine command, Map environment) throws ExecuteException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(CommandLine command, ExecuteResultHandler handler) throws ExecuteException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(CommandLine command, Map environment, ExecuteResultHandler handler) throws ExecuteException, IOException {
        throw new UnsupportedOperationException();
    }
}
