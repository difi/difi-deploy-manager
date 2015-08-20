package no.difi.deploymanager.download.dao;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.util.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.io.IOException;

/***
 * DownloadDao will retrieve the list over applications that is ready to download from remote repository.
 * The download list is updated by CheckVersion in scheduler.
 *
 * @see no.difi.deploymanager.versioncheck.service.CheckVersionService
 * @see no.difi.deploymanager.schedule.Scheduler
 */
@Repository
public class DownloadDao {
    private final Environment environment;
    private final IOUtil ioUtil;

    @Autowired
    public DownloadDao(Environment environment, IOUtil ioUtil) {
        this.environment = environment;
        this.ioUtil = ioUtil;
    }

    public void saveDownloadList(ApplicationList forDownload) throws IOException {
        ioUtil.saveApplicationList(
                forDownload,
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.fordownload.file")
        );
    }

    public ApplicationList retrieveDownloadList() throws IOException {
        return ioUtil.retrieveApplicationList(
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.fordownload.file")
        );
    }
}
