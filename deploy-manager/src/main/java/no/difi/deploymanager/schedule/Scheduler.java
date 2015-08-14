package no.difi.deploymanager.schedule;

import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.download.service.DownloadService;
import no.difi.deploymanager.versioncheck.service.CheckVersionService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Scheduler {
    public static final String CRON_RUN_CHECK_FOR_VERSION = "0 5 * * * MON-FRI";
    public static final String CRON_RUN_DOWNLOAD_NEW_VERSION = "30 * * * * MON-FRI";
    public static final String CRON_RESTART_APPLICATIONS = "30 * * * * MON-FRI";
    public static final String CRON_RUN_EVERY_1MIN = "1 * * * * MON-FRI";

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

    @Scheduled(cron = CRON_RUN_CHECK_FOR_VERSION)
    public void checkForNewVersion() {
        DateTime start = new DateTime();

        logStatus(checkVersionService.execute());

        DateTime stop = new DateTime();
        Duration duration = new Duration(start, stop);

        logger.log(Level.INFO, String.format("Checking for new versions took %d sec to run.", duration.getStandardSeconds()));
    }

    @Scheduled(cron = CRON_RUN_DOWNLOAD_NEW_VERSION)
    public void downloadNewVersion() {
        DateTime start = new DateTime();

        logStatus(downloadService.execute());

        DateTime stop = new DateTime();
        Duration duration = new Duration(start, stop);

        logger.log(Level.INFO, String.format("Checking for new versions took %d sec to run.", duration.getStandardSeconds()));
    }

    @Scheduled(cron = CRON_RESTART_APPLICATIONS)
    public void restartApplications() {
        DateTime start = new DateTime();

        logStatus(restartService.execute());

        DateTime stop = new DateTime();
        Duration duration = new Duration(start, stop);

        logger.log(Level.INFO, String.format("Checking for new versions took %d sec to run.", duration.getStandardSeconds()));
    }

    private void logStatus(List<Status> result) {
        Level logLevel;
        for (Status status : result) {
            if (status.getStatusCode() == StatusCode.SUCCESS) {
                logLevel = Level.INFO;
            } else if (status.getStatusCode() == StatusCode.ERROR) {
                logLevel = Level.ERROR;
            } else {
                logLevel = Level.FATAL;
            }
            logger.log(logLevel, status.getDescription());
        }
    }
}
