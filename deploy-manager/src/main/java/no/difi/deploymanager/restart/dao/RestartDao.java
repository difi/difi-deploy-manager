package no.difi.deploymanager.restart.dao;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Self;
import no.difi.deploymanager.util.IOUtil;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.io.IOException;

/***
 * RestartDao retrieve the list over applications that is downloaded and ready for restart.
 * The list over applications to restart is updated by Download.
 *
 * @see no.difi.deploymanager.download.service.DownloadService
 * @see no.difi.deploymanager.schedule.Scheduler
 */
@Repository
public class RestartDao {
    private final Environment environment;
    private final IOUtil ioUtil;

    public RestartDao(Environment environment, IOUtil ioUtil) {
        this.environment = environment;
        this.ioUtil = ioUtil;
    }

    public void saveRestartList(ApplicationList restartList) throws IOException {
        System.out.println("***Saving restart list");
        System.out.println("***Path: " + environment.getRequiredProperty("monitoring.base.path"));
        System.out.println("***File: " + environment.getRequiredProperty("monitoring.fordownload.file"));
        if (restartList != null) {
            System.out.println("***Elements: " + restartList.getApplications().size());
            for (ApplicationData data : restartList.getApplications()) {
                System.out.println("*****Filename: " + data.getFilename());
                System.out.println("*****Group id: " + data.getGroupId());
                System.out.println("*****Artifact id: " + data.getArtifactId());
                System.out.println("*****Start params: " + data.getStartParameters());
                System.out.println("*****Active version: " + data.getActiveVersion());
            }
        }
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

    public Self fetchSelfVersion() {
        return ioUtil.getVersion();
    }
}
