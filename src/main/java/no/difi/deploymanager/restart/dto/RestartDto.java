package no.difi.deploymanager.restart.dto;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Self;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import no.difi.deploymanager.util.IOUtil;

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
            String startCommand = "java -jar " + System.getProperty("user.dir") + environment.getProperty("download.base.path") + "/" + processToStart.getFilename();

            Process process = Runtime.getRuntime().exec(new String[]{ROOT_PATH_FOR_SH, "-c", startCommand});
            return process.isAlive();
        } catch (IOException e) {
            return false;
        }
    }

    public boolean stopProcess(ApplicationData oldVersion) {
        String processId;
        final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        try {
            String killCommand = "";
            if (isWindows) {
                killCommand = "taskkill /pid ";
            }
            else {
                killCommand = "kill 9 ";
            }

            processId = findProcessId(oldVersion);
            if (!isEmpty(processId)) {
                Process process = Runtime.getRuntime().exec(killCommand + processId);
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
        final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        List<String> runningProcesses = findProcess(version, isWindows);

        for (String running : runningProcesses) {
            if (!isEmpty(running)) {
                if (running.contains(version.getFilename()) && !running.contains(ROOT_PATH_FOR_SH)) {
                    List<String> processParts = asList(running.split(" "));

                    if (isWindows) {
                        return processParts.get(1);
                    }
                    else {
                        return processParts.get(0);
                    }
                }
            }
        }

        return "";
    }

    private List<String> findProcess(ApplicationData oldVersion, boolean isWindows) throws IOException, InterruptedException {
        Process process = null;
        List<String> processes = new ArrayList<>();
        String line;

        if (isWindows) {
            String[] findWindowsApp = new String[] {
                    "tasklist /v /fo csv",
                    "| findstr /i" + oldVersion.getFilename()

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

        do {
            line = stdout.readLine();
            processes.add(line);
        }
        while (line != null);

        process.waitFor();
        process.destroy();

        return processes;
    }

    public Self findAppVersion() {
        return ioUtil.getVersion();
    }
}
