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
