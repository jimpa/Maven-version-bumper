/*
 * Copyright (c) 2012 Jim Svensson <jimpa@tla.se>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package se.tla.mavenversionbumper;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.Diff;

import java.io.File;

import static org.junit.Assert.*;

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
     * @param sourceModuleName Name of source to load.
     * @param resultReference Name of the expected reference.
     * @param tinker Tinker to use.
     * @throws Exception In case of problems.
     */
    public static void template(String sourceModuleName, String resultReference, ModuleTinker tinker) throws Exception {
        File base = new File("target/test-classes/sources");
        File moduleBase = new File(base, sourceModuleName);
        File pomFile = new File(moduleBase, "pom.xml");
        String backup = FileUtils.readFileToString(pomFile);
        Module subject = new Module("target/test-classes/sources", sourceModuleName);
        tinker.tink(subject);
        subject.save();
        String result = FileUtils.readFileToString(pomFile);
        FileUtils.writeStringToFile(pomFile, backup);
        String reference = FileUtils.readFileToString(new File("target/test-classes/references", resultReference));

        Diff diff = new Diff(reference, result);

        if (! diff.identical()) {
            fail(diff.toString());
        }
    }
}
