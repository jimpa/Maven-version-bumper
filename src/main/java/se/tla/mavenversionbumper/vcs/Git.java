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

import org.apache.commons.exec.CommandLine;
import se.tla.mavenversionbumper.Module;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Implements VersionControl for the Git versioning system.
 *
 * Requires access to the command line interface git.
 */
public class Git extends AbstractVersionControl {

    private static final String COMMANDPATH = "git.path";
    private static final String COMMANDPATHDEFAULT = "git";

    private final String commandPath;

    public Git(Properties controlProperties) {
        String commandProperty = controlProperties.getProperty(COMMANDPATH, COMMANDPATHDEFAULT);
        if (System.getProperty("os.name").toLowerCase().contains("windows") &&
                commandProperty.toLowerCase().endsWith(".exe")) {
            commandProperty += ".exe";
        }
        commandPath = commandProperty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore(List<Module> modules) {
        for (Module module : modules) {
            File parentDir = module.pomFile().getParentFile();

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("file", module.pomFile().getName());

            CommandLine cmdLine = new CommandLine(commandPath);
            cmdLine.addArgument("checkout");
            cmdLine.addArgument("${file}");
            cmdLine.setSubstitutionMap(map);

            execute(cmdLine, parentDir);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit(List<Module> modules) {
        for (Module module : modules) {

            if (!module.pomFile().exists()) {
                throw new IllegalArgumentException("File to commit does not exist.");
            }

            File parentDir = module.pomFile().getParentFile();

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("file", module.pomFile().getName());
            map.put("message", module.commitMessage());

            CommandLine cmdLine = new CommandLine(commandPath);
            cmdLine.addArgument("commit");
            cmdLine.addArgument("-m");
            cmdLine.addArgument("${message}");
            cmdLine.addArgument("${file}");
            cmdLine.setSubstitutionMap(map);

            execute(cmdLine, parentDir);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void label(List<Module> modules) {
        for (Module module : modules) {
            String label = module.label();
            if (label != null && label.length() > 0) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("tag", label);

                CommandLine cmdLine = new CommandLine(commandPath);
                cmdLine.addArgument("tag");
                cmdLine.addArgument("-f");
                cmdLine.addArgument("${tag}");
                cmdLine.setSubstitutionMap(map);

                execute(cmdLine, module.pomFile().getParentFile());
            }
        }
    }
}
