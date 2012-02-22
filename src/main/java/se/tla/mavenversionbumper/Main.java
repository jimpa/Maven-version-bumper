package se.tla.mavenversionbumper;

import bsh.EvalError;
import bsh.Interpreter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.jdom.JDOMException;
import se.tla.mavenversionbumper.vcs.AbstractVersionControl;
import se.tla.mavenversionbumper.vcs.Clearcase;
import se.tla.mavenversionbumper.vcs.Git;
import se.tla.mavenversionbumper.vcs.VersionControl;

import java.io.*;
import java.util.*;

/**
 * Command line interface for the version bumper.
 */
public class Main {

    public static final List<Module> loadedModules = new LinkedList<Module>();

    private static String baseDir;
    private static String scenarioFileName;
    private static VersionControl versionControl;
    private static Map<String, Class<? extends VersionControl>> versionControllers;

    static {
        versionControllers = new HashMap<String, Class<? extends VersionControl>>();
        // Only lower case names.
        versionControllers.put("git", Git.class);
        versionControllers.put("clearcase", Clearcase.class);
    }

    public static void main(String args[]) {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: <base directory> <scenarioFile> [<VC properties file>]");
            System.exit(1);
        }
        baseDir = args[0];
        scenarioFileName = args[1];
        Properties versionControlProperties = null;

        if (args.length == 3) {
            try {
                String versionControlParameter = args[2];
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

        try {
            String scenario = FileUtils.readFileToString(scenarioFile);
            Interpreter i = new Interpreter();
            i.eval("importCommands(\"se.tla.mavenversionbumper.commands\")");
            i.eval("import se.tla.mavenversionbumper.Main");
            i.eval("import se.tla.mavenversionbumper.Module");
            i.eval("baseDir = \"" + baseDir + "\"");
            i.eval("load(String moduleName) { return Main.load(moduleName, true); }");
            i.eval("loadReadOnly(String moduleName) { return Main.load(moduleName, false); }");
            i.eval("saveLoadedModules() { Main.saveLoadedModules(); }");
            i.eval(scenario);
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a Module located by this filename that is a directory relative to the baseDir.
     *
     * If the Module is opened for updating, it is saved during a call ti the saveLoadedModules().
     *
     * @param moduleDirectoryName
     * @param openForUpdate
     * @return Newly created Module.
     * @throws JDOMException
     * @throws IOException
     */
    public static Module load(String moduleDirectoryName, boolean openForUpdate) throws JDOMException, IOException {
        Module m = new Module(baseDir, moduleDirectoryName, versionControl);

        if (openForUpdate) {
            loadedModules.add(m);
        }
        return m;
    }

    /**
     * Call save() on all Modules loaded by the load() method that was opened for update.
     * @throws IOException
     */
    public static void saveLoadedModules() throws IOException {
        for (Module m : loadedModules) {
            m.save();
        }
    }
}
