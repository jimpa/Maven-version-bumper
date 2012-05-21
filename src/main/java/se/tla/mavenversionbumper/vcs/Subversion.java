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
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.io.IOUtils;
import se.tla.mavenversionbumper.Module;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Implements VersionControl for the Subversion versioning system.
 *
 * Requires access to the command line interface svn.
 */
public class Subversion extends AbstractVersionControl {
    public static final String ACRONYM = "subversion";

    protected static final String COMMANDPATH = "svn.path";
    private static final String COMMANDPATHDEFAULT = "svn";
    private static final String REPOSITORY_ROOT = "Repository Root: ";
    private final String commandPath;

    protected static final String TAGSBASE = "svn.tagsbase";
    private static final String TAGSBASEDEFAULT = "tags";
    private final String tagsBase;

    protected static final String ALTREPOBASE = "svn.alternaterepositorybase";
    private final String alternateRepositoryBase;

    public Subversion(Properties controlProperties) {
        String commandProperty = controlProperties.getProperty(COMMANDPATH, COMMANDPATHDEFAULT);
        if (System.getProperty("os.name").toLowerCase().contains("windows") &&
                ! commandProperty.toLowerCase().endsWith(".exe")) {
            commandProperty += ".exe";
        }
        commandPath = commandProperty;

        tagsBase = controlProperties.getProperty(TAGSBASE, TAGSBASEDEFAULT);
        alternateRepositoryBase = controlProperties.getProperty(ALTREPOBASE);
    }

    @Override
    public void restore(List<Module> modules) {
        for (Module module : modules) {
            Map<String, Object> map = new HashMap<String, Object>();

            CommandLine cmdLine = new CommandLine(commandPath);
            cmdLine.addArgument("revert");

            cmdLine.addArgument("${file}");
            map.put("file", module.pomFile());

            cmdLine.setSubstitutionMap(map);
            execute(cmdLine, null);
        }
    }

    @Override
    public void commit(List<Module> modules) {
        for (Module module : modules) {
            Map<String, Object> map = new HashMap<String, Object>();

            CommandLine cmdLine = new CommandLine(commandPath);
            cmdLine.addArgument("commit");

            if (module.commitMessage() != null) {
                cmdLine.addArgument("-m").addArgument("${message}");
                map.put("message", module.commitMessage());
            } else {
                cmdLine.addArgument("-m").addArgument("");
            }

            cmdLine.addArgument("${file}");
            map.put("file", module.pomFile());

            cmdLine.setSubstitutionMap(map);
            execute(cmdLine, null);
        }
    }

    @Override
    public void label(List<Module> modules) {

        for (Module module : modules) {
            String label = module.label();
            if (label != null) {
                String repositoryUrl = extractRepositoryUrl(module);
                Map<String, Object> map = new HashMap<String, Object>();

                CommandLine cmdLine = new CommandLine(commandPath);
                cmdLine
                        .addArgument("copy")
                        .addArgument("-m").addArgument("");

                cmdLine.addArgument("${src}");
                if (alternateRepositoryBase == null) {
                    map.put("src", module.pomFile().getParentFile());
                } else {
                    map.put("src", repositoryUrl + "/" + alternateRepositoryBase);
                }

                cmdLine.addArgument("${dest}");
                map.put("dest", repositoryUrl + "/" + tagsBase + "/" + label);

                cmdLine.setSubstitutionMap(map);
                execute(cmdLine, null);
            }
        }
    }

    private String extractRepositoryUrl(Module module) {
        CommandLine cmdLine = new CommandLine(commandPath);
        cmdLine.addArgument("info").addArgument("${file}");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("file", module.pomFile());
        cmdLine.setSubstitutionMap(map);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ExecuteStreamHandler streamHandler = new ExposingPumpStreamHandler(bos);
        try {
            execute(cmdLine, null, -1, streamHandler);

            List<String> lines = IOUtils.readLines(new ByteArrayInputStream(bos.toByteArray()), "ISO-8859-1");

            for (String line : lines) {
                if (line.startsWith(REPOSITORY_ROOT)) {
                    return line.substring(REPOSITORY_ROOT.length());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        throw new IllegalStateException("No repository URL could be found for: " + module.ga());
    }
}
