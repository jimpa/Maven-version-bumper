package se.tla.mavenversionbumper;

import bsh.EvalError;
import bsh.Interpreter;
import org.apache.commons.io.FileUtils;
import org.jdom.JDOMException;
import se.tla.mavenversionbumper.vcs.Clearcase;
import se.tla.mavenversionbumper.vcs.VersionControl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
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
        versionControllers.put("clearcase", Clearcase.class);
    }

    public static void main(String args[]) {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: <base directory> <scenarioFile> [<version control>]");
            System.exit(1);
        }
        baseDir = args[0];
        scenarioFileName = args[1];
        if (args.length == 3) {
            String versionControlName = args[2].toLowerCase();
            Class<? extends VersionControl> versionControlClass = versionControllers.get(versionControlName);
            if (versionControlClass == null) {
                System.err.println("No such version control: " + versionControlName);
                System.exit(1);
            }
            try {
                versionControl = versionControlClass.newInstance();
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
            i.eval("load(String moduleName) { return Main.load(moduleName); }");
            i.eval("saveLoadedModules() { Main.saveLoadedModules(); }");
            i.eval(scenario);
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getBaseDir() {
        return baseDir;
    }

    public static void setBaseDir(String baseDir) {
        Main.baseDir = baseDir;
    }

    public static Module load(String filename) throws JDOMException, IOException {
        Module m = new Module(baseDir, filename, versionControl);

        loadedModules.add(m);
        return m;
    }

    public static void saveLoadedModules() throws IOException {
        for (Module m : loadedModules) {
            m.save();
        }
    }
}
