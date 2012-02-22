package se.tla.mavenversionbumper;

import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jimpa
 * Date: 2/22/12
 * Time: 10:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleTest {

    @Test
    public void testSimple() throws Exception {
        Module subject = new Module("src/test/resources", "simple", null);

        Assert.assertEquals("se.tla.maven", subject.groupId());
        Assert.assertEquals("versionbumper", subject.artifactId());
        Assert.assertEquals("1.0-SNAPSHOT", subject.version());
    }
}
