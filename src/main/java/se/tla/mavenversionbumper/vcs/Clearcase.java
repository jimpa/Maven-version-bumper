package se.tla.mavenversionbumper.vcs;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jimpa
 * Date: 2/14/12
 * Time: 11:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class Clearcase implements VersionControl {

    private final Set<String> checkedOut = new HashSet<String>();

    @Override
    public void prepareSave(String fileName) {
        if (! checkedOut.contains(fileName)) {
            // TODO Checkout
            checkedOut.add(fileName);
        }
    }

    @Override
    public void checkin(String fileName) {
        // TODO checkin...
        checkedOut.remove(fileName);
    }

    @Override
    public void label(String label, String directory) {
        // TODO label...
    }
}
