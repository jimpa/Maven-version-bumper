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

import se.tla.mavenversionbumper.Module;

import java.util.List;

/**
 * A placebo version control system.
 */
public class NoopVersionControl implements VersionControl {
    @Override
    public String before(List<Module> modules) {
        // Do nothing.
        return null;
    }

    @Override
    public String after(List<Module> modules) {
        // Do nothing.
        return null;
    }

    @Override
    public void restore(List<Module> modules) {
        // Do nothing.
    }

    @Override
    public void commit(List<Module> modules) {
        // Do nothing.
    }

    @Override
    public void label(List<Module> modules) {
        // Do nothing.
    }
}
