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

package se.tla.mavenversionbumper.vcs;

import se.tla.mavenversionbumper.ReadonlyModule;

import java.io.File;

/**
 * Subclass of Module used to test implementations of the VersionControl interface.
 */
public class TestableModule extends ReadonlyModule {
    File pomFile;
    String commitMessage;
    String label;

    public TestableModule(File pomFile, String group, String artifact, String version, String commitMessage, String label) {
        super(group, artifact, version);
        this.pomFile = pomFile;
        this.commitMessage = commitMessage;
        this.label = label;
    }

    @Override
    public String commitMessage() {
        return commitMessage;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public File pomFile() {
        return pomFile;
    }

    @Override
    public boolean labelOnlyPomXml() {
        return false;
    }
}
