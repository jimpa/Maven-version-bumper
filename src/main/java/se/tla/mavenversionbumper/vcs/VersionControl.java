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
 * Defines common functionality needed from a Version Control System.
 */
public interface VersionControl {

    /**
     * Called before any work is done in the Version Control.
     * @param modules Modules to prepare for.
     * @return Output to print back to the caller. null means nothing to print.
     */
    String before(List<Module> modules);

    /**
     * Called after all other work is done in the Version Control.
     * @param modules Modules that has been handled by the Version Control.
     * @return Output to print back to the caller. null means nothing to print.
     */
    String after(List<Module> modules);

    /**
     * Use the Version Control System to revert any changes made to these modules.
     *
     * This method only backs out changes that haven't been commited and it does not
     * affect any labels that has been applied.
     *
     * @param modules Revert changes for these modules.
     */
    void restore(List<Module> modules);

    /**
     * Commit these modules to the Version Control System.
     * @param modules Modules.
     */
    void commit(List<Module> modules);

    /**
     * Apply any labels that has been registered in these modules.
     * @param modules Modules.
     */
    void label(List<Module> modules);
}
