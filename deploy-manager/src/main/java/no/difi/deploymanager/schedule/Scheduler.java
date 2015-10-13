package no.difi.deploymanager.schedule;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/***
 * Scheduler trigger check for version, download and restart by set intervals and logs processing and responses from the services.
 *
 * @see no.difi.deploymanager.versioncheck.service.CheckVersionService
 * @see no.difi.deploymanager.download.service.DownloadService
 * @see no.difi.deploymanager.restart.service.RestartService
 */
@Component
public class Scheduler {
    private static final String CRON_RUN_CHECK_FOR_VERSION = "0 5 * * * MON-FRI";
    private static final String CRON_RUN_DOWNLOAD_NEW_VERSION = "30 * * * * MON-FRI";
    private static final String CRON_RESTART_APPLICATIONS = "30 * * * * MON-FRI";
    private static final String CRON_RUN_EVERY_10MIN = "*/10 * * * * MON-FRI";
    private static final String CRON_RUN_EVERY_HOUR = "0 0 0/1 * * MON-FRI";

    private final CheckVersionService checkVersionService;
    private final DownloadService downloadService;
    private final RestartService restartService;

    private static final Logger logger = LogManager.getLogger(Scheduler.class);

    @Autowired
    public Scheduler(CheckVersionService checkVersionService, DownloadService downloadService, RestartService restartService) {
        this.checkVersionService = checkVersionService;
        this.downloadService = downloadService;
        this.restartService = restartService;
    }

    @Scheduled(cron = CRON_RUN_EVERY_10MIN)
    public void checkForNewVersion() {
        DateTime start = new DateTime();

        Common.logStatus(checkVersionService.execute(), logger);

        DateTime stop = new DateTime();
        Duration duration = new Duration(start, stop);

        logger.log(Level.INFO, String.format("Checking for new versions took %d sec to run.", duration.getStandardSeconds()));
    }

    @Scheduled(cron = CRON_RUN_EVERY_10MIN)
    public void downloadNewVersion() {
        DateTime start = new DateTime();

        Common.logStatus(downloadService.execute(), logger);

        DateTime stop = new DateTime();
        Duration duration = new Duration(start, stop);

        logger.log(Level.INFO, String.format("Checking for new versions took %d sec to run.", duration.getStandardSeconds()));
    }

    @Scheduled(cron = CRON_RUN_EVERY_10MIN)
    public void restartApplications() {
        DateTime start = new DateTime();

        Common.logStatus(restartService.execute(), logger);

        DateTime stop = new DateTime();
        Duration duration = new Duration(start, stop);

        logger.log(Level.INFO, String.format("Checking for new versions took %d sec to run.", duration.getStandardSeconds()));
    }
}
