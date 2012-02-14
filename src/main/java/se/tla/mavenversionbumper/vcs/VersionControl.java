package se.tla.mavenversionbumper.vcs;

/**
 * Created by IntelliJ IDEA.
 * User: jimpa
 * Date: 2/14/12
 * Time: 11:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VersionControl {

    void prepareSave(String fileName);

    void checkin(String fileName);

    void label(String label, String directory);

}
