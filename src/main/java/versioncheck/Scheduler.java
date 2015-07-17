package versioncheck;

import domain.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import versioncheck.service.CheckVersionService;

import java.util.List;

@Component
public class Scheduler {
    public static final String CRON_RUN_5AM_WEEKDAYS = "0 5 * * * MON-FRI";
    public static final String CRON_RUN_EVERY_1MIN = "1 * * * * MON-FRI";

    CheckVersionService checkVersionService;

    @Autowired
    public Scheduler(CheckVersionService checkVersionService) {
        this.checkVersionService = checkVersionService;
    }


    @Scheduled(cron = CRON_RUN_EVERY_1MIN)
    public void checkForNewVersion() {
        List<Status> result = checkVersionService.execute();
        System.out.println(result);
    }

    public void downloadNewVersion() {

    }

    public void installNewVersion() {

    }

    public void restartNewVersion() {

    }
}
