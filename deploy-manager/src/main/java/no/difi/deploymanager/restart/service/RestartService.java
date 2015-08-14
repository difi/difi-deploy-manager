package no.difi.deploymanager.restart.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.versioncheck.dto.CheckVersionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import no.difi.deploymanager.restart.dto.RestartDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Service
public class RestartService {
    private final RestartDto restartDto;
    private final CheckVersionDto checkVersionDto;

    private List<Status> statuses = new ArrayList<>();

    @Autowired
    public RestartService(RestartDto restartDto, CheckVersionDto checkVersionDto) {
        this.restartDto = restartDto;
        this.checkVersionDto = checkVersionDto;
    }

    public List<Status> execute() {
        try {
            ApplicationList restartList = restartDto.retrieveRestartList();
            ApplicationList runningAppList = checkVersionDto.retrieveRunningAppsList();
            ApplicationData applicationWithNewVersion;

            if (restartList != null && restartList.getApplications() != null) {
                for (ApplicationData newApp : restartList.getApplications()) {
                    applicationWithNewVersion = null;
                    if (runningAppList != null) {
                        applicationWithNewVersion = findAppForRestart(runningAppList, newApp);
                    }
                    try {
                        if (applicationWithNewVersion != null) {
                            boolean result = restartDto.executeRestart(applicationWithNewVersion, newApp);
                            if (result) {
                                statuses.add(new Status(
                                        StatusCode.SUCCESS,
                                        format("Application %s updated to version %s",
                                                newApp.getArtifactId(), newApp.getActiveVersion())));
                            }
                        }
                        else {
                            boolean result = restartDto.startProcess(newApp);
                            if (result) {
                                statuses.add(new Status(
                                        StatusCode.SUCCESS,
                                        String.format("%s with version %s is started.", newApp.getArtifactId(), newApp.getActiveVersion())
                                ));
                            }
                        }
                    } catch (InterruptedException e) {
                        statuses.add(new Status(StatusCode.ERROR, format("Could not execute restart on %s with version %s. Attempting to start up old version (%s).", newApp.getName(), newApp.getActiveVersion(), applicationWithNewVersion.getActiveVersion())));
                        boolean result = restartDto.startProcess(applicationWithNewVersion);

                        if (result) {
                            statuses.add(new Status(StatusCode.SUCCESS, format("Restarted old version %s with success.", applicationWithNewVersion.getActiveVersion())));
                        } else {
                            statuses.add(new Status(StatusCode.ERROR, format("Could not start up %s, %s. Application is not running!", applicationWithNewVersion.getName(), applicationWithNewVersion.getActiveVersion())));
                        }
                    }
                }
            }
        } catch (IOException e) {
            statuses.add(new Status(StatusCode.ERROR, "Local error when retrieving list of applications to restart"));
        }
        return statuses;
    }

    private ApplicationData findAppForRestart(ApplicationList runningAppList, ApplicationData newApp) {
        ApplicationData appWithNewVersion = null;
        for (ApplicationData oldApp : runningAppList.getApplications()) {
            if (newApp.getGroupId().equals(oldApp.getGroupId()) && newApp.getArtifactId().equals(oldApp.getArtifactId())) {
                appWithNewVersion = oldApp;
                break;
            }
        }
        return appWithNewVersion;
    }
}
