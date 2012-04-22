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

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class ReverseEngineerTest {

    @Test
    public void testReverseEngineering() throws Exception {
        File baseDir = new File("src/test/resources/reverse-engineer");
        File resultFile = File.createTempFile("result", "file", new File("target/test-classes"));
        File expectedResultFile = new File("src/test/resources/references/reverseengineering.bsh");

        List<ReverseEngineeringModule> modules =  Main.findModulesForReverseEngineering(baseDir, "");

        Main.reverseEngineerModules(modules, resultFile);

        String result = FileUtils.readFileToString(resultFile);
        String expectedResult = FileUtils.readFileToString(expectedResultFile);
        Assert.assertEquals(expectedResult, result);
    }
}
