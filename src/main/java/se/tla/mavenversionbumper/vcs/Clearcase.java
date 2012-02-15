package se.tla.mavenversionbumper.vcs;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements VersionControl for the Clearcase versioning system.
 *
 * Requires access to the command line interface cleartool.
 */
public class Clearcase extends AbstractVersionControl implements VersionControl {

    private final Set<String> checkedOut = new HashSet<String>();

    @Override
    public void prepareSave(String fileName) {
        if (! checkedOut.contains(fileName)) {
            // TODO Checkout
            System.out.println("Clearcase: checkout");
            checkedOut.add(fileName);
        }
    }

    @Override
    public void checkin(String fileName) {
        // TODO checkin...
        System.out.println("Clearcase: checkin");
        checkedOut.remove(fileName);
    }

    @Override
    public void label(String label, String directory) {
        System.out.println("Clearcase: label");
        // TODO Create label type
        // TODO label...
    }
}
