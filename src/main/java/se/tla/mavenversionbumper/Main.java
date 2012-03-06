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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.jdom.JDOMException;

import se.tla.mavenversionbumper.vcs.AbstractVersionControl;
import se.tla.mavenversionbumper.vcs.Clearcase;
import se.tla.mavenversionbumper.vcs.Git;
import se.tla.mavenversionbumper.vcs.VersionControl;
import bsh.EvalError;
import bsh.Interpreter;

/**
 * Command line interface for the version bumper.
 */
public class Main {

    public static final List<Module> modulesLoadedForUpdate = new LinkedList<Module>();

    private static String baseDir;
    private static VersionControl versionControl;
    private static Map<String, Class<? extends VersionControl>> versionControllers;

    static {
        versionControllers = new HashMap<String, Class<? extends VersionControl>>();
        // Only lower case names.
        versionControllers.put("git", Git.class);
        versionControllers.put("clearcase", Clearcase.class);
    }

    enum Option {
        DRYRUN("Dry run. Don't modify anything, only validate configuration.", "d", "dry-run"),
        REVERT("Revert any uncommited changes.", "r", "revert"),
        HELP("Show help.", "h", "?", "help");

        private String helpText;
        private String[] aliases;

        Option(String helpText, String... aliases) {
            this.helpText = helpText;
            this.aliases = aliases;
        }

        public List<String> getAliases() {
            return Arrays.asList(aliases);
        }

        public String getHelpText() {
            return helpText;
        }

        public boolean presentIn(OptionSet o) {
            return o.has(aliases[0]);
        }
    }

    enum TYPE {
        NORMAL, DRYRUN, REVERT
    }

    public static void main(String args[]) {

        OptionParser parser = new OptionParser() {
            {
                acceptsAll(Option.DRYRUN.getAliases(), Option.DRYRUN.getHelpText());
                acceptsAll(Option.REVERT.getAliases(), Option.REVERT.getHelpText());
                acceptsAll(Option.HELP.getAliases(), Option.HELP.getHelpText());
            }
        };

        OptionSet options = null;
        try {
            options = parser.parse(args);
        } catch (OptionException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (Option.HELP.presentIn(options)) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e) {
                System.err.println("Error printing help text: " + e.getMessage());
                System.exit(1);
            }
            System.exit(0);
        }

        if (Option.DRYRUN.presentIn(options) && Option.REVERT.presentIn(options)) {
            System.err.println("Only one of --dry-run/-d and --revert/-r");
            System.exit(1);
        }

        List<String> arguments = options.nonOptionArguments();

        if (arguments.size() < 2 || arguments.size() > 3) {
            System.err.println("Usage: [-d | --dry-run] [-r | --revert] [-h | --help] <base directory> <scenarioFile> [<VC properties file>]");
            System.exit(1);
        }

        baseDir = arguments.get(0);
        String scenarioFileName = arguments.get(1);
        Properties versionControlProperties;

        if (arguments.size() == 3) {
            try {
                String versionControlParameter = arguments.get(2);
                versionControlProperties = new Properties();
                versionControlProperties.load(new AutoCloseInputStream(new FileInputStream(versionControlParameter)));

                String versionControlName = versionControlProperties.getProperty(AbstractVersionControl.VERSIONCONTROL, "").toLowerCase();
                Class<? extends VersionControl> versionControlClass = versionControllers.get(versionControlName);
                if (versionControlClass == null) {
                    System.err.println("No such version control: " + versionControlName);
                    System.exit(1);
                }

                versionControl = versionControlClass.getConstructor(Properties.class).newInstance(versionControlProperties);
            } catch (Exception e) {
                System.err.println("Error starting up the version control");
                e.printStackTrace();
                System.exit(1);
            }
        }
        File scenarioFile = new File(scenarioFileName);
        if (!(scenarioFile.isFile() && scenarioFile.canRead())) {
            System.err.println("Scenario file " + scenarioFileName + " isn't a readable file.");
            System.exit(1);
        }

        if (Option.REVERT.presentIn(options) && versionControl == null) {
            System.err.println("Version control has to be defined while reverting.");
            System.exit(1);
        }

        TYPE type = TYPE.NORMAL;
        if (Option.DRYRUN.presentIn(options)) {
            type = TYPE.DRYRUN;
        }
        if (Option.REVERT.presentIn(options)) {
            type = TYPE.REVERT;
        }

        try {
            String scenario = FileUtils.readFileToString(scenarioFile);
            Interpreter i = new Interpreter();
            i.eval("importCommands(\"se.tla.mavenversionbumper.commands\")");
            i.eval("import se.tla.mavenversionbumper.Main");
            i.eval("import se.tla.mavenversionbumper.Module");
            i.eval("baseDir = \"" + baseDir + "\"");
            i.eval("load(String moduleName) { return Main.load(moduleName, null, null, true); }");
            i.eval("load(String moduleName, String newVersion, String label) { return Main.load(moduleName, newVersion, label, true); }");
            i.eval("loadReadOnly(String moduleName) { return Main.load(moduleName, null, null, false); }");
            i.eval("loadReadOnly(String moduleName, String version) { return Main.load(moduleName, version, null, false); }");
            i.eval(scenario);

            if (type.equals(TYPE.NORMAL)) {
                if (versionControl != null) {
                    // Prepare for saving
                    for (Module module : modulesLoadedForUpdate) {
                        versionControl.prepareSave(module);
                    }
                }

                // Save
                for (Module module : modulesLoadedForUpdate) {
                    module.save();
                }

                if (versionControl != null) {
                    // Commit
                    for (Module module : modulesLoadedForUpdate) {
                        versionControl.commit(module);
                    }

                    // Label
                    versionControl.label(modulesLoadedForUpdate);
                }
            }

            if (type.equals(TYPE.REVERT)) {
                if (versionControl != null) {
                    for (Module module : modulesLoadedForUpdate) {
                        versionControl.restore(module);
                    }
                }
            }
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a Module located by this filename that is a directory relative to the baseDir.
     *
     * @param moduleDirectoryName
     * @param newVersion
     * @param label
     * @param openForUpdate
     * @return Newly created Module.
     * @throws JDOMException
     * @throws IOException
     */
    public static Module load(String moduleDirectoryName, String newVersion, String label, boolean openForUpdate) throws JDOMException, IOException {
        Module m = new Module(baseDir, moduleDirectoryName);

        if (newVersion != null) {
            System.out.println("ORG: " + m.gav());
            m.version(newVersion);
        }
        if (label != null) {
            m.label(label);
        }

        if (newVersion != null) {
            System.out.println("NEW: " + m.gav() + (label != null ? " (" + label + ")" : ""));
        }

        if (openForUpdate) {
            modulesLoadedForUpdate.add(m);
        }

        return m;
    }
}
