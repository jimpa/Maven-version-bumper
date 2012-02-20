package se.tla.mavenversionbumper.vcs;

import org.apache.commons.exec.CommandLine;

import java.io.File;
import java.util.*;

/**
 * Implements VersionControl for the Git versioning system.
 *
 * Requires access to the command line interface git.
 */
public class Git extends AbstractVersionControl {

    public static final String COMMANDPATH = "git.path";

    private final String commandPath;

    public Git(Properties controlProperties) {
        this.commandPath = controlProperties.getProperty(COMMANDPATH);
        if (commandPath == null) {
            throw new IllegalArgumentException("No " + COMMANDPATH + " defined for git executable");
        }

        if (! new File(commandPath).exists()) {
            throw new IllegalArgumentException(COMMANDPATH + " " + commandPath + " doesn't exist");
        }
    }

    @Override
    public void commit(File file, String message) {
        if (! file.exists()) {
            throw new IllegalArgumentException("File to commit does not exist.");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("The commmit message must contain something.");
        }

        File parentDir = file.getParentFile();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("file", file.getName());
        map.put("message", message);

        CommandLine cmdLine = new CommandLine(commandPath);
        cmdLine.addArgument("commit");
        cmdLine.addArgument("-m");
        cmdLine.addArgument("${message}");
        cmdLine.addArgument("${file}");
        cmdLine.setSubstitutionMap(map);

        execute(cmdLine, parentDir);
    }

    @Override
    public void label(String label, File ... targets) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tag", label);

        for (File target : targets) {
            //map.put("file", target.getName());

            CommandLine cmdLine = new CommandLine(commandPath);
            cmdLine.addArgument("tag");
            cmdLine.addArgument("-f");
            cmdLine.addArgument("${tag}");
            cmdLine.setSubstitutionMap(map);

            execute(cmdLine, target.getParentFile());
        }
    }
}
