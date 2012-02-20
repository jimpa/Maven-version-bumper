package se.tla.mavenversionbumper.vcs;

import java.io.File;

/**
 * Defines common functionality needed from a Version Control System.
 */
public interface VersionControl {

    /**
     * This file is about to be written. Please prepare.
     * @param file file to be written.
     */
    void prepareSave(File file);

    /**
     * Commit this file into the VCS.
     * @param file file to check in.
     * @param message Commit message. Can't be null or empty.
     * @throws IllegalArgumentException If message is null or empty, or if fileName doesn't point to an existing file.
     */
    void commit(File file, String message);

    /**
     * Apply this label recursively to these file targets.
     * @param label
     * @param targets
     */
    void label(String label, File ... targets);

}
