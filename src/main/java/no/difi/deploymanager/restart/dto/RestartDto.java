package no.difi.deploymanager.restart.dto;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Self;
import no.difi.deploymanager.util.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
public class RestartDto {
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private static String ROOT_PATH_FOR_SH = "/bin/sh";

    private final Environment environment;
    private final IOUtil ioUtil;

    @Autowired
    public RestartDto(Environment environment, IOUtil ioUtil) {
        this.environment = environment;
        this.ioUtil = ioUtil;
    }

    public void saveRestartList(ApplicationList restartList) throws IOException {
        ioUtil.saveApplicationList(
                restartList,
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.forrestart.file"));
    }

    public ApplicationList retrieveRestartList() throws IOException {
        return ioUtil.retrieveApplicationList(
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.forrestart.file"));
    }

    public boolean executeRestart(ApplicationData oldVersion, ApplicationData newVersion) throws IOException, InterruptedException {
        Self thisApp = findAppVersion();
        if (oldVersion.getName().equals(thisApp.getName())
                && !oldVersion.getActiveVersion().contains(thisApp.getVersion())) {
            //Have to be opposite from normal restart when self.
            startProcess(newVersion);
            return stopProcess(oldVersion);
        }

        stopProcess(oldVersion);
        return startProcess(newVersion);
    }

    public boolean startProcess(ApplicationData processToStart) {
        try {
            if (IS_WINDOWS) {
                String[] startCommand = new String[] {"java", "-jar",
                        (System.getProperty("user.dir") + environment.getProperty("download.base.path") + "/" + processToStart.getFilename()).replace("/", "\\")};
                Runtime.getRuntime().exec(startCommand);
            }
            else {
                String startCommand = "java -jar " + System.getProperty("user.dir") + environment.getProperty("download.base.path") + "/" + processToStart.getFilename();
                Runtime.getRuntime().exec(new String[]{ROOT_PATH_FOR_SH, "-c", startCommand});
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean stopProcess(ApplicationData oldVersion) {
        String processId;
        try {
            String killCommand;
            if (IS_WINDOWS) {
                killCommand = "taskkill /F /pid ";
            }
            else {
                killCommand = "kill 9 ";
            }

            processId = findProcessId(oldVersion);
            if (!isEmpty(processId)) {
                Process process = Runtime.getRuntime().exec(killCommand + processId.replace("\"", ""));
                process.waitFor();
                process.destroy();
                return true;
            }
            return false;

        } catch (IOException io) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    private String findProcessId(ApplicationData version) throws IOException, InterruptedException {
        List<String> runningProcesses = findProcess(version);
        String processIdPart = "";

        for (String running : runningProcesses) {
            if (!isEmpty(running)) {
                if (IS_WINDOWS) {
                    List<String> processParts = asList(asList(running.split(" ")).get(0).split(","));

                    //When windows, we have to do a bit more to find the correct process.
                    String pid = processParts.get(1);

                    String command = "wmic process where processid=" + pid.replace("\"", "") + " get commandline";
                    Process checkProcess = Runtime.getRuntime().exec(command);

                    InputStream input = checkProcess.getInputStream();
                    BufferedReader stdout = new BufferedReader(new InputStreamReader(input));

                    String line;
                    do {
                        line = stdout.readLine();
                        if (line != null && line.contains(version.getFilename())) {
                            processIdPart = pid;
                            break;
                        }
                    }
                    while (line != null);

                    checkProcess.waitFor();
                    checkProcess.destroy();

                    if (!isEmpty(processIdPart)) {
                        return processIdPart;
                    }
                }
                else {
                    if (running.contains(version.getFilename()) && !running.contains(ROOT_PATH_FOR_SH)) {
                        List<String> processParts = asList(running.split(" "));

                        // Only one process will be fuond when nix-based system.
                        return processParts.get(0);
                    }
                }
            }
        }

        return "";
    }

    private List<String> findProcess(ApplicationData oldVersion) throws IOException, InterruptedException {
        Process process = null;
        List<String> output = new ArrayList<>();

        if (IS_WINDOWS) {
            String[] findWindowsApp = new String[] {
                    "tasklist",
                    "/v",
                    "/fo",
                    "csv"
            };
            process = Runtime.getRuntime().exec(findWindowsApp);
        }
        else {
            String[] findUnixApp = new String[] {
                    ROOT_PATH_FOR_SH,
                    "-c",
                    "ps -ax | grep java | grep " + oldVersion.getFilename()
            };

            process = Runtime.getRuntime().exec(findUnixApp);
        }

        InputStream input = process.getInputStream();
        BufferedReader stdout = new BufferedReader(new InputStreamReader(input));
        String line;

        do {
            line = stdout.readLine();
            if (IS_WINDOWS && line != null && line.contains("java.exe") || !IS_WINDOWS) {
                output.add(line);
            }
        }
        while (line != null);

        process.waitFor();
        process.destroy();

        return output;
    }

    public Self findAppVersion() {
        return ioUtil.getVersion();
    }
}
