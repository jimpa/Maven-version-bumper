package se.tla.mavenversionbumper.vcs;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;


import org.apache.commons.exec.CommandLine;
import se.tla.mavenversionbumper.Module;

/**
 * Implements VersionControl for the Clearcase versioning system.
 *
 * Requires access to the command line interface cleartool.
 */
public class Clearcase extends AbstractVersionControl {
    public static final String ACRONYM = "clearcase";

    protected static final String COMMANDPATH = "cleartool.path";
    private static final String COMMANDPATHDEFAULT = "cleartool";
    protected static final String LABELTIMEOUT = "cleartool.labeltimeout";
    private static final String LABELDEFAULTTIMEOUT = "900000"; // 15 minutes.
    protected static final String CHECKOUTRESERVED = "cleartool.checkoutreserved";
    private static final String CHECKOUTRESERVEDDEFAULT = "true";

    private final Set<Module> checkedOut = new HashSet<Module>();
    private final String commandPath;
    private final int labelTimeout;
    private final boolean checkoutReserved;

    public Clearcase(Properties controlProperties) {
        String commandProperty = controlProperties.getProperty(COMMANDPATH, COMMANDPATHDEFAULT);
        if (System.getProperty("os.name").toLowerCase().contains("windows") &&
                ! commandProperty.toLowerCase().endsWith(".exe")) {
            commandProperty += ".exe";
        }
        commandPath = commandProperty;

        String timeoutProperty = controlProperties.getProperty(LABELTIMEOUT, LABELDEFAULTTIMEOUT);
        try {
            this.labelTimeout = Integer.parseInt(timeoutProperty);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The property " + LABELTIMEOUT + " must be an integer");
        }

        String checkoutReservedProperty = controlProperties.getProperty(CHECKOUTRESERVED, CHECKOUTRESERVEDDEFAULT);
        this.checkoutReserved = Boolean.parseBoolean(checkoutReservedProperty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void before(List<Module> modules) {
        for (Module module: modules) {
            checkout(module);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void after(List<Module> modules) {
        StringBuilder sb = new StringBuilder();
        for (Module module : modules) {
            String label = module.label();
            if (label != null && label.length() > 0) {
                sb.append("element * ").append(label).append("\n");
            }
        }
        if (sb.length() > 0) {
            System.out.println("element * CHECKEDOUT");
            System.out.print(sb.toString());
            System.out.println("element * /main/LATEST");
        }
    }

    /**
     * Perform a checkout of the specific modules pom.xml file.
     * @param module Module to perform the checkout for.
     */
    private void checkout(Module module) {
        if (! checkedOut.contains(module)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("file", module.pomFile());

            CommandLine cmdLine = new CommandLine(commandPath);
            cmdLine.addArgument("checkout");
            if (checkoutReserved) {
                cmdLine.addArgument("-reserved");
            } else {
                cmdLine.addArgument("-unreserved");
            }
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
    public void restore(List<Module> modules) {
        for (Module module : modules) {
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit(List<Module> modules) {
        for (Module module : modules) {
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void label(List<Module> modules) {
        List<Module> modulesToLabel = new LinkedList<Module>();
        for (Module module : modules) {
            String label = module.label();
            if (label != null && label.length() > 0) {
                modulesToLabel.add(module);
            }
        }

        // Remove duplicate label names.
        Set<String> labels = new TreeSet<String>();
        for (Module module : modulesToLabel) {
            labels.add(module.label());
        }

        // Create label types.
        for (String label : labels) {
            mklbtype(label);
        }

        // Apply the labels to the modules.
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
