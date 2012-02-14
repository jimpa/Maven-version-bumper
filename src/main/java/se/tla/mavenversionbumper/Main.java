package se.tla.mavenversionbumper;

import bsh.EvalError;
import bsh.Interpreter;
import org.apache.commons.io.FileUtils;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class Main {

    public static final List<Module> loadedModules = new LinkedList<Module>();

    private static String baseDir;
    private static String scenarioFileName;

    public static void main(String args[]) throws IOException, JDOMException, EvalError {
        if (args.length != 2) {
            System.err.println("Usage: <base directory> <scenarioFileName bean shell file>");
            System.exit(1);
        }
        baseDir = args[0];
        scenarioFileName = args[1];

        File scenarioFile = new File(scenarioFileName);
        if (!(scenarioFile.isFile() && scenarioFile.canRead())) {
            System.err.println("Scenario file " + scenarioFileName + " isn't a readable file.");
            System.exit(1);
        }

        String scenario = FileUtils.readFileToString(scenarioFile);
        Interpreter i = new Interpreter();
        i.eval("importCommands(\"se.tla.mavenversionbumper.commands\")");
        i.eval("import se.tla.mavenversionbumper.Main");
        i.eval("import se.tla.mavenversionbumper.Module");
        i.eval("baseDir = \"" + baseDir + "\"");
        i.eval("load(String moduleName) { return Main.load(moduleName); }");
        i.eval(scenario);

        if (true) {
            return;
        }

        Module base = load("/");
        base.version("1.2");

        Module moduleA = load("moduleA");
        moduleA.parentVersion(base);
        moduleA.version("9.1-SNAPSHOT");

        Module moduleB = load("moduleB");
        moduleB.parentVersion(base);
        moduleB.updateDependency(moduleA);
        moduleB.version("3.1");

        Module moduleC = load("moduleC");
        moduleC.parentVersion(base);
        moduleC.updatePluginDependency(moduleB);
        moduleC.version("3.1");

        Module moduleD = load("moduleD");
        moduleD.parentVersion(base);
        moduleD.updatePluginDependency(moduleB);
        moduleD.version("3.2");

        saveLoadedModules();

        if (true) {
            return;
        }


        Module cobol2JavaPlugin = load("Cobol2JavaPlugin");
        cobol2JavaPlugin.parentVersion(base);
        cobol2JavaPlugin.version("1.2");

        Module cobol2Java = load("Cobol2Java");
        cobol2Java.parentVersion(base);
        cobol2Java.version("1.1");
        cobol2Java.updatePluginDependency(cobol2JavaPlugin);

        Module TAC = load("TAC");
        TAC.parentVersion(base);
        TAC.version("1.0");
        TAC.updateDependency(cobol2Java);

        Module kontor = load("Kontor");
        kontor.parentVersion(base);
        kontor.version("1.1");
        kontor.updateDependency(TAC);

        Module kontorEjb = load("Kontor/ejb");
        kontorEjb.parentVersion(kontor);

        Module kontorWar = load("Kontor/war");
        kontorWar.parentVersion(kontor);

        Module kontorEar = load("Kontor/ear");
        kontorEar.parentVersion(kontor);

        saveLoadedModules();
    }

    public static String getBaseDir() {
        return baseDir;
    }

    public static void setBaseDir(String baseDir) {
        Main.baseDir = baseDir;
    }

    public static Module load(String filename) throws JDOMException, IOException {
        Module m = new Module(baseDir, filename, null);

        loadedModules.add(m);
        return m;
    }

    public static void saveLoadedModules() throws IOException {
        for (Module m : loadedModules) {
            m.save();
        }
    }
}
