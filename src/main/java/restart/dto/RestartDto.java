package restart.dto;

import domain.ApplicationData;
import domain.ApplicationList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import util.IOUtil;

import java.io.IOException;
import java.util.List;

@Repository
public class RestartDto {
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
                environment.getRequiredProperty("monitoring.forupdate.file")
        );
    }

    public ApplicationList retrieveRestartList() throws IOException {
        return ioUtil.retrieveApplicationList(
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.forupdate.file")
        );
    }

    public void executeRestart(ApplicationData applicationData) throws IOException {
//        List<Process> runningProcesses = ioUtil.retrieveRunningProcesses();
//
//        String appPath = "java -jar "
//                + System.getProperty("user.dir")
//                + environment.getProperty("download.base.path")
//                + applicationData.getFilename();
//
//        Process exec = Runtime.getRuntime().exec("java -jar " + System.getProperty("user.dir") + "/deplowrapper-test/target/deploy-wrapper-test-0.9.0-TEST.jar");
//
//        TODO: Check address localhost:9999/health. Response should be: {"status":"UP"}
//
//        exec.destroy();
//        System.out.println(exec.isAlive());
    }
}
