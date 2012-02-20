package se.tla.mavenversionbumper.vcs;

import org.apache.commons.exec.CommandLine;

import java.io.File;
import java.util.*;

/**
 * Implements VersionControl for the Clearcase versioning system.
 *
 * Requires access to the command line interface cleartool.
 */
public class Clearcase extends AbstractVersionControl {

    public static final String COMMANDPATH = "cleartool.path";

    private final Set<String> checkedOut = new HashSet<String>();
    private final String commandPath;

    public Clearcase(Properties controlProperties) {
        this.commandPath = controlProperties.getProperty(COMMANDPATH);
        if (commandPath == null) {
            throw new IllegalArgumentException("No " + COMMANDPATH + " defined for cleartool executable");
        }

        if (! new File(commandPath).exists()) {
            throw new IllegalArgumentException(COMMANDPATH + " " + commandPath + " doesn't exist");
        }
    }

    /**
     * Make sure that the file is checked out before the actual save.
     * @param file pom.xml to save.
     */
    @Override
    public void prepareSave(File file) {
        if (! checkedOut.contains(file.getName())) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("file", file.getName());

            CommandLine cmdLine = new CommandLine(commandPath);
            cmdLine.addArgument("checkout");
            cmdLine.addArgument("-c");
            cmdLine.addArgument("\"\"");
            cmdLine.addArgument("${file}");
            cmdLine.setSubstitutionMap(map);

            execute(cmdLine, null);

            checkedOut.add(file.getName());
        }
    }

    @Override
    public void commit(File file, String message) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("file", file.getName());
        map.put("comment", message);

        CommandLine cmdLine = new CommandLine(commandPath);
        cmdLine.addArgument("checkin");
        cmdLine.addArgument("-c");
        cmdLine.addArgument("${comment}");
        cmdLine.addArgument("${file}");
        cmdLine.setSubstitutionMap(map);

        execute(cmdLine, null);

        checkedOut.remove(file.getName());
    }

    /**
     * This assumes that the label has to be created as well as applied.
     * @param label
     * @param targets
     */
    @Override
    public void label(String label, File ... targets) {
        // Create label type
        mklbtype(label);
        // Label
        mklabel(label, targets);
    }

    private void mklbtype(String label) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("label", label);

        CommandLine cmdLine = new CommandLine(commandPath);
        cmdLine.addArgument("mklbtype");
        cmdLine.addArgument("-c");
        cmdLine.addArgument("\"\"");
        cmdLine.addArgument("${label}");
        cmdLine.setSubstitutionMap(map);

        execute(cmdLine, null);
    }

    private void mklabel(String label, File ... targets) {
        int index = 0;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("label", label);

        CommandLine cmdLine = new CommandLine(commandPath);
        cmdLine.addArgument("mklabel");
        cmdLine.addArgument("${label}");

        for (File target : targets) {
            index++;
            String argname = "file" + index;
            map.put(argname, target);
            cmdLine.addArgument("${" + argname + "}");
        }

        cmdLine.setSubstitutionMap(map);

        execute(cmdLine, null);
    }
}
