package se.tla.mavenversionbumper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Unit tests for the Module class.
 */
public class ModuleTest {

    @Test
    public void testSimple() throws Exception {
        Module subject = new Module("target/test-classes", "simple", null);

        Assert.assertEquals("se.tla.maven", subject.groupId());
        Assert.assertEquals("versionbumper", subject.artifactId());
        Assert.assertEquals("1.0-SNAPSHOT", subject.version());
        Assert.assertEquals("se.tla.maven:versionbumper:1.0-SNAPSHOT", subject.gav());
    }

    @Test
    public void testParentVersion() throws Exception {
        Module subject = new Module("target/test-classes", "withparent", null);

        Assert.assertEquals("0.1-SNAPSHOT", subject.parentVersion());
    }

    @Test
    public void testItemsFromParent() throws Exception {
        Module subject = new Module("target/test-classes", "withparent", null);

        Assert.assertEquals("se.tla.maven", subject.groupId());
        Assert.assertEquals("0.1-SNAPSHOT", subject.version());
    }

    @Test
    public void testParentVersionWithNoParent() throws Exception {
        Module subject = new Module("target/test-classes", "simple", null);

        Assert.assertNull(subject.parentVersion());

        try {
            subject.parentVersion("1.0");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            Module parent = new Module("target/test-classes", "simple", null);
            subject.parentVersion(parent);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testParentMissingVersion() throws Exception {
        Module subject = new Module("target/test-classes/withparent", "missingversion", null);

        try {
            subject.parentVersion("1.0");
            Assert.fail();
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test
    public void testSave() throws Exception {
        Module subject = new Module("target/test-classes", "simple", null);

        Assert.assertEquals("1.0-SNAPSHOT", subject.version());

        subject.version("2.0");
        subject.save();

        Module testSubject = new Module("target/test-classes", "simple", null);
        Assert.assertEquals("2.0", testSubject.version());
    }
}
