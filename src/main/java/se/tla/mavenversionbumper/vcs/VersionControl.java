package se.tla.mavenversionbumper.vcs;

/**
 * Defines common functionality needed from a Version Control System.
 */
public interface VersionControl {

    /**
     * This file is about to be written. Please prepare.
     * @param fileName file to be written.
     */
    void prepareSave(String fileName);

    /**
     * Check in this file into the VCS.
     * @param fileName file to check in.
     */
    void checkin(String fileName);

    /**
     * Apply this label recursively to this file tree.
     * @param label
     * @param directory
     */
    void label(String label, String directory);

}
