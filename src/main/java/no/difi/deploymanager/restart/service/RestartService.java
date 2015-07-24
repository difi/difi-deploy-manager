package no.difi.deploymanager.restart.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
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

    private List<Status> statuses = new ArrayList<>();

    @Autowired
    public RestartService(RestartDto restartDto) {
        this.restartDto = restartDto;
    }

    public List<Status> execute() {
        try {
            ApplicationList restartList = restartDto.retrieveRestartList();
            ApplicationList runningAppList = null;
            ApplicationData oldAppData;

            if (restartList != null && restartList.getApplications() != null) {
                for (ApplicationData newApp : restartList.getApplications()) {
                    oldAppData = null;
                    if (runningAppList != null) {
                        for (ApplicationData oldApp : runningAppList.getApplications()) {
                            if (newApp.getName().getGroupId().equals(oldApp.getName().getGroupId()) && newApp.getName().getArtifactId().equals(oldApp.getName().getArtifactId())) {
                                oldAppData = oldApp;
                                break;
                            }
                        }
                    }
                    try {
                        boolean result = restartDto.executeRestart(oldAppData, newApp);
                        if (result) {
                            statuses.add(new Status(StatusCode.SUCCESS, format("Application %s updated to version %s", newApp.getName().getArtifactId(), newApp.getActiveVersion())));
                        }
                    } catch (InterruptedException e) {
                        statuses.add(new Status(StatusCode.ERROR, format("Could not execute no.difi.deploymanager.restart on %s with version %s. Attempting to start up old version (%s).", newApp.getName(), newApp.getActiveVersion(), oldAppData.getActiveVersion())));
                        boolean result = restartDto.startProcess(oldAppData);

                        if (result) {
                            statuses.add(new Status(StatusCode.SUCCESS, format("Restarted old version %s with success.", oldAppData.getActiveVersion())));
                        } else {
                            statuses.add(new Status(StatusCode.ERROR, format("Could not start up %s, %s. Application is not running!", oldAppData.getName(), oldAppData.getActiveVersion())));
                        }
                    }
                }
            }
        } catch (IOException e) {
            statuses.add(new Status(StatusCode.ERROR, "Local error when retrieving list of applications to restart"));
        }
        return statuses;
    }
}
