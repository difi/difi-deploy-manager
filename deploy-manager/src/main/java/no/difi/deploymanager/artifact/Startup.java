package no.difi.deploymanager.artifact;

import no.difi.deploymanager.download.service.DownloadService;
import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.util.Common;
import no.difi.deploymanager.versioncheck.service.CheckVersionService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Startup {
    private final CheckVersionService checkVersionService;
    private final DownloadService downloadService;
    private final RestartService restartService;

    private static final Logger logger = LogManager.getLogger(Startup.class);

    @Autowired
    public Startup(CheckVersionService checkVersionService, DownloadService downloadService, RestartService restartService) {
        this.checkVersionService = checkVersionService;
        this.downloadService = downloadService;
        this.restartService = restartService;
    }

    public void runOnStartup() {
        DateTime start = new DateTime();

        Common.logStatus(checkVersionService.execute(), logger);
        Common.logStatus(downloadService.execute(), logger);
        Common.logStatus(restartService.execute(), logger);

        DateTime stop = new DateTime();
        Duration duration = new Duration(start, stop);

        logger.log(Level.INFO, String.format("Checking for new versions took %d sec to run.", duration.getStandardSeconds()));
    }
}