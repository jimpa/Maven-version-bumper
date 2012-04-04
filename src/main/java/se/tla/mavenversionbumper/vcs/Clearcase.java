package se.tla.mavenversionbumper.vcs;

import java.io.File;
import java.util.*;

import org.apache.commons.exec.CommandLine;
import se.tla.mavenversionbumper.Module;

/**
 * Implements VersionControl for the Clearcase versioning system.
 *
 * Requires access to the command line interface cleartool.
 */
public class Clearcase extends AbstractVersionControl {

    public static final String COMMANDPATH = "cleartool.path";
    public static final String LABELTIMEOUT = "cleartool.labeltimeout";
    private static final String LABELDEFAULTTIMEOUT = "900000"; // 15 minutes.

    private final Set<Module> checkedOut = new HashSet<Module>();
    private final String commandPath;
    private final int labelTimeout;

    public Clearcase(Properties controlProperties) {
        this.commandPath = controlProperties.getProperty(COMMANDPATH);
        if (commandPath == null) {
            throw new IllegalArgumentException("No " + COMMANDPATH + " defined for cleartool executable");
        }

        if (! new File(commandPath).exists()) {
            throw new IllegalArgumentException(COMMANDPATH + " " + commandPath + " doesn't exist");
        }
        String timeoutProperty = controlProperties.getProperty(LABELTIMEOUT, LABELDEFAULTTIMEOUT);

        try {
            this.labelTimeout = Integer.parseInt(timeoutProperty);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The property " + LABELTIMEOUT + " must be an integer");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareSave(Module module) {
        if (! checkedOut.contains(module)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("file", module.pomFile());

            CommandLine cmdLine = new CommandLine(commandPath);
            cmdLine.addArgument("checkout");
            cmdLine.addArgument("-nc");
            cmdLine.addArgument("${file}");
            cmdLine.setSubstitutionMap(map);

            execute(cmdLine, null);

            checkedOut.add(module);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore(Module module) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("file", module.pomFile());

        CommandLine cmdLine = new CommandLine(commandPath);
        cmdLine.addArgument("uncheckout");
        cmdLine.addArgument("-rm");
        cmdLine.addArgument("${file}");
        cmdLine.setSubstitutionMap(map);

        execute(cmdLine, null);

        checkedOut.remove(module);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit(Module module) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("file", module.pomFile());

        CommandLine cmdLine = new CommandLine(commandPath);
        cmdLine.addArgument("checkin");
        String message = module.commitMessage();
        if (message != null && message.length() > 0) {
        	cmdLine.addArgument("-c");
        	cmdLine.addArgument("${comment}");
        	map.put("comment", message);
        } else {
            cmdLine.addArgument("-nc");
        }
        cmdLine.addArgument("${file}");
        cmdLine.setSubstitutionMap(map);

        execute(cmdLine, null);

        checkedOut.remove(module);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void label(Collection<Module> modules) {
        Collection<Module> modulesToLabel = new LinkedList<Module>();
        for (Module module : modules) {
            String label = module.label();
            if (label != null && label.length() > 0) {
                modulesToLabel.add(module);
            }
        }

        // Create label types
        for (Module module : modulesToLabel) {
            mklbtype(module.label());
        }

        // Label
        for (Module module : modulesToLabel) {
            if (module.labelOnlyPomXml()) {
                mklabel(module.label(), false, module.pomFile(), module.pomFile().getParentFile());
            } else {
                mklabel(module.label(), true, module.pomFile().getParentFile());
            }
        }
    }

    private void mklbtype(String label) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("label", label);

        CommandLine cmdLine = new CommandLine(commandPath);
        cmdLine.addArgument("mklbtype");
        cmdLine.addArgument("-nc");
        cmdLine.addArgument("${label}");
        cmdLine.setSubstitutionMap(map);

        execute(cmdLine, null);
    }

    private void mklabel(String label, boolean recurse, File ... targets) {
        int index = 0;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("label", label);

        CommandLine cmdLine = new CommandLine(commandPath);
        cmdLine.addArgument("mklabel");
        if (recurse) {
            cmdLine.addArgument("-recurse");
        }
        cmdLine.addArgument("-replace");
        cmdLine.addArgument("-nc");
        cmdLine.addArgument("${label}");

        for (File target : targets) {
            index++;
            String argname = "file" + index;
            map.put(argname, target);
            cmdLine.addArgument("${" + argname + "}");
        }

        cmdLine.setSubstitutionMap(map);

        execute(cmdLine, null, labelTimeout);
    }
}
