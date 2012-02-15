package se.tla.mavenversionbumper.vcs;

/**
 * An implementation that does nothing.
 */
public class NoopVC implements VersionControl {
    @Override
    public void prepareSave(String fileName) {
        // Do nothing.
    }

    @Override
    public void checkin(String fileName) {
        // Do nothing.
    }

    @Override
    public void label(String label, String directory) {
        // Do nothing.
    }
}
