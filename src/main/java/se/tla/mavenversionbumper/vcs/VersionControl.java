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

import java.util.Collection;

/**
 * Defines common functionality needed from a Version Control System.
 */
public interface VersionControl {

    /**
     * This module is about to be written. Please prepare.
     * @param module Module.
     */
    void prepareSave(Module module);

    /**
     * Use the Version Control System to revert any changes made to this module.
     *
     * This method only backs out changes that havn't been commited and it does not
     * affect any labels that has been applied.
     *
     * @param module Revert changes for this module.
     */
    void restore(Module module);

    /**
     * Commit these modules to the Version Control System.
     * @param module Module.
     */
    void commit(Module module);

    /**
     * Apply any labels that has been registered in these modules.
     * @param modules Modules.
     */
    void label(Module ... modules);

    /**
     * Apply any labels that has been registered in these modules.
     * @param modules Modules.
     */
    void label(Collection<Module> modules);
}
