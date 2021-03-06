package no.difi.deploymanager.restart.dao;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.difi.deploymanager.util.Common.IS_WINDOWS;
import static org.springframework.util.StringUtils.isEmpty;

/***
 * Class handle start, stop and restart of applications for both Linux and Windows operating systems.
 */
public class RestartCommandLine {
    private static final Logger logger = LoggerFactory.getLogger(RestartCommandLine.class);
    private static final String ROOT_PATH_FOR_SH = "/bin/sh";

    private final Environment environment;

    public RestartCommandLine(Environment environment) {
        this.environment = environment;
    }

    /***
     * Execute restart by stopping application defined in oldVersion and starting newVersion.
     *
     * @param oldVersion Application data for the application to stop.
     * @param newVersion Application data for the application to start.
     * @param self       Used to check if application to restart is current app.
     * @return Returns true if restart is successful, otherwise false.
     */
    public boolean executeRestart(ApplicationData oldVersion, ApplicationData newVersion, Self self) {
        if (oldVersion.getName().equals(self.getName())
                && !oldVersion.getActiveVersion().contains(self.getVersion())) {
            //Have to be opposite from normal restart when self.
            startProcess(newVersion);
            return stopProcess(oldVersion);
        }

        logger.info("Performing restart on {} ", oldVersion.getName());
        stopProcess(oldVersion);
        return startProcess(newVersion);
    }

    /***
     * Starting application defined in processToStart.
     *
     * @param processToStart Contains data about the application to start.
     * @return true if starting application were successful, otherwise false.
     */
    public boolean startProcess(ApplicationData processToStart) {
        try {
            logger.info("Starting {}", processToStart.getFilename());
            if (IS_WINDOWS) {
                String startCommand = "javaw -jar ";
                String fileWithPath = (System.getProperty("user.dir") + environment.getProperty("download.base.path")
                        + "/" + processToStart.getFilename()).replace("/", "\\");

                doStart(startCommand
                        + processToStart.getEnvironmentVariables() + " "
                        + fileWithPath + " "
                        + processToStart.getMainClass() + " "
                        + processToStart.getVmOptions()
                );
            } else {
                String startCommand = "java -jar ";
                String fileWithPath = System.getProperty("user.dir")
                        + environment.getProperty("download.base.path") + "/" + processToStart.getFilename();

                doStart(startCommand
                        + processToStart.getEnvironmentVariables() + " "
                        + fileWithPath + " "
                        + processToStart.getMainClass() + " "
                        + processToStart.getVmOptions()
                );
            }

            return true;
        } catch (IOException e) {
            logger.error("Failed to start process", e);
            return false;
        }
    }

    /***
     * Stopping application defined in processToStop
     *
     * @param processToStop Contains data for the process to stop.
     * @return true if stopping process were successful, otherwise false.
     */
    public boolean stopProcess(ApplicationData processToStop) {
        logger.info("Stopping {} ", processToStop.getFilename());
        String processId;
        try {
            String killCommand;
            if (IS_WINDOWS) {
                killCommand = "taskkill /F /pid ";
            } else {
                killCommand = "kill 9 ";
            }

            processId = findProcessId(processToStop);
            if (!isEmpty(processId)) {
                Process process = Runtime.getRuntime().exec(killCommand + processId.replace("\"", ""));
                process.waitFor();
                process.destroy();

                return true;
            }
            logger.error("Failed to stop application {} ", processToStop.getFilename());
            return false;
        } catch (IOException io) {
            logger.error("Failed to stop process", io);
            return false;
        } catch (InterruptedException e) {
            logger.error("Failed to stop process", e);
            return false;
        }
    }

    public String findProcessId(ApplicationData version) throws IOException, InterruptedException {
        List<String> runningProcesses = findProcess(version);

        for (String running : runningProcesses) {
            if (!isEmpty(running)) {
                if (IS_WINDOWS) {
                    List<String> processParts = asList(asList(running.split(" ")).get(0).split(","));

                    //When windows, we have to do a bit more to find the correct process.
                    String pid = processParts.get(1);

                    String command = "wmic process where processid=" + pid.replace("\"", "") + " get commandline";
                    Process checkProcess = Runtime.getRuntime().exec(command);

                    InputStream input = checkProcess.getInputStream();
                    BufferedReader stdout = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

                    String processIdPart = findPidOnWinOS(version, pid, stdout);

                    checkProcess.waitFor();
                    checkProcess.destroy();

                    if (!isEmpty(processIdPart)) {
                        return processIdPart;
                    }
                } else {
                    List<String> processParts = asList(running.split(" "));
                    // Give process time to destroy itself.
                    Thread.sleep(500);
                    if (running.contains(version.getFilename())) {
                        // Only one process will be fuond when nix-based system.
                        return processParts.get(0);
                    }
                }
            }
        }

        return "";
    }

    private void doStart(String startCommand) throws IOException {
        Process process = Runtime.getRuntime().exec(startCommand);
        setUpStreamThread(process.getInputStream(), new PrintStream(new FileOutputStream("deploymanager_output.log"), true, StandardCharsets.UTF_8.name()));
        setUpStreamThread(process.getErrorStream(), new PrintStream(new FileOutputStream("deploymanager_error.log"), true, StandardCharsets.UTF_8.name()));
    }

    private static void setUpStreamThread(final InputStream inputStream, final PrintStream printStream) {
        final InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        new Thread(new Runnable() {
            public void run() {
                BufferedReader br = new BufferedReader(streamReader);
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        printStream.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private String findPidOnWinOS(ApplicationData version, String pid, BufferedReader stdout) throws IOException {
        String line;
        do {
            line = stdout.readLine();
            if (line != null && line.contains(version.getFilename())) {
                return pid;
            }
        }
        while (line != null);
        return null;
    }

    private List<String> findProcess(ApplicationData oldVersion) throws IOException, InterruptedException {
        Process process;
        List<String> output = new ArrayList<>();

        if (IS_WINDOWS) {
            String[] findWindowsApp = new String[]{
                    "tasklist", "/v", "/fo", "csv"
            };
            process = Runtime.getRuntime().exec(findWindowsApp);
        } else {
            String[] findUnixApp = new String[]{
                    ROOT_PATH_FOR_SH,
                    "-c", "ps -ax | grep java | grep " + oldVersion.getFilename() + " | grep -v grep"
            };

            process = Runtime.getRuntime().exec(findUnixApp);
        }

        InputStream input = process.getInputStream();
        try (BufferedReader stdout = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
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
    }
}
