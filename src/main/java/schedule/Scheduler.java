package schedule;

import domain.Status;
import download.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import versioncheck.service.CheckVersionService;

import java.util.List;

@Component
public class Scheduler {
    public static final String CRON_RUN_CHECK_FOR_VERSION = "0 5 * * * MON-FRI";
    public static final String CRON_RUN_DOWNLOAD_NEW_VERSION = "30 * * * * MON-FRI";
    public static final String CRON_RUN_EVERY_1MIN = "1 * * * * MON-FRI";

    private final CheckVersionService checkVersionService;
    private final DownloadService downloadService;

    @Autowired
    public Scheduler(CheckVersionService checkVersionService, DownloadService downloadService) {
        this.checkVersionService = checkVersionService;
        this.downloadService = downloadService;
    }


    @Scheduled(cron = CRON_RUN_EVERY_1MIN)
    public void checkForNewVersion() {
        List<Status> result = checkVersionService.execute();
        //TODO: Result from checkVersionService needs to be logged.
    }

    @Scheduled(cron = CRON_RUN_EVERY_1MIN)
    public void downloadNewVersion() {
        List<Status> result = downloadService.execute();
        //TODO: Result from downloadService needs to be logged.
    }

    public void installNewVersion() {

    }

    public void restartNewVersion() {

    }
}
