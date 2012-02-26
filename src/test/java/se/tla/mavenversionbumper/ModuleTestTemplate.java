package se.tla.mavenversionbumper;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;

import java.io.File;

/**
 * Helper class to ease the testing of transformation in the Module class.
 */
class ModuleTestTemplate {

    /**
     * Automates most of the procedure around testing how the Module class behaves.
     *
     * <ul>
     *     <li>Load a Module with a pom.xml from a module in .../resources/sources</li>
     *     <li>Pass it to the supplied Tinker</li>
     *     <li>Save the result</li>
     *     <li>Use XMLUnit to verify the result with a reference XML-file from .../resources/references</li>
     *     <li>Restore the original input file</li>
     * </ul>
     *
     * @param sourceModuleName
     * @param resultReference
     * @param tinker
     * @throws Exception
     */
    public static void template(String sourceModuleName, String resultReference, ModuleTinker tinker) throws Exception {
        File base = new File("target/test-classes/sources");
        File moduleBase = new File(base, sourceModuleName);
        File pomFile = new File(moduleBase, "pom.xml");
        String backup = FileUtils.readFileToString(pomFile);
        Module subject = new Module("target/test-classes/sources", sourceModuleName, null);
        tinker.tink(subject);
        subject.save();
        String result = FileUtils.readFileToString(pomFile);
        FileUtils.writeStringToFile(pomFile, backup);
        String reference = FileUtils.readFileToString(new File("target/test-classes/references", resultReference));

        Diff diff = new Diff(reference, result);

        if (! diff.identical()) {
            Assert.fail(diff.toString());
        }
    }
}
