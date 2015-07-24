package schedule;

import domain.Status;
import download.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import restart.service.RestartService;
import versioncheck.service.CheckVersionService;

import java.util.List;

@Component
public class Scheduler {
    public static final String CRON_RUN_CHECK_FOR_VERSION = "0 5 * * * MON-FRI";
    public static final String CRON_RUN_DOWNLOAD_NEW_VERSION = "30 * * * * MON-FRI";
    public static final String CRON_RESTART_APPLICATIONS = "30 * * * * MON-FRI";
    public static final String CRON_RUN_EVERY_1MIN = "1 * * * * MON-FRI";

    private final versioncheck.service.CheckVersionService checkVersionService;
    private final download.service.DownloadService downloadService;
    private final restart.service.RestartService restartService;

    @Autowired
    public Scheduler(CheckVersionService checkVersionService, DownloadService downloadService, RestartService restartService) {
        this.checkVersionService = checkVersionService;
        this.downloadService = downloadService;
        this.restartService = restartService;
    }


    @Scheduled(cron = CRON_RUN_CHECK_FOR_VERSION)
    public void checkForNewVersion() {
        List<Status> result = checkVersionService.execute();
        //TODO: Result from checkVersionService needs to be logged.
    }

    @Scheduled(cron = CRON_RUN_DOWNLOAD_NEW_VERSION)
    public void downloadNewVersion() {
        List<Status> result = downloadService.execute();
        //TODO: Result from downloadService needs to be logged.
    }

    @Scheduled(cron = CRON_RUN_EVERY_1MIN)
    public void restartApplications() {
        List<Status> result = restartService.execute();
    }
}
