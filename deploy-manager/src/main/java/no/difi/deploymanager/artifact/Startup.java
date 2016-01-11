package no.difi.deploymanager.artifact;

import no.difi.deploymanager.download.service.DownloadService;
import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.versioncheck.service.CheckVersionService;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Startup {
    private final CheckVersionService checkVersionService;
    private final DownloadService downloadService;
    private final RestartService restartService;

    private static final Logger logger = LoggerFactory.getLogger(Startup.class);

    public Startup(CheckVersionService checkVersionService, DownloadService downloadService, RestartService restartService) {
        this.checkVersionService = checkVersionService;
        this.downloadService = downloadService;
        this.restartService = restartService;
    }

    public void runOnStartup() {
        DateTime start = new DateTime();

        checkVersionService.execute();
        downloadService.execute();
        restartService.execute();

        DateTime stop = new DateTime();
        Duration duration = new Duration(start, stop);

        logger.info("Checking for new versions took {} sec to run.", duration.getStandardSeconds());
    }

    public void forceStop() {
        logger.info("Deploy manager stopped. Closing applications.");

        restartService.stopRunningApplications();
    }
}