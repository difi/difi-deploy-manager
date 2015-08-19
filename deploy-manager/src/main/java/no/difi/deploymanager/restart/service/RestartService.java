package no.difi.deploymanager.restart.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.restart.dao.RestartCommandLine;
import no.difi.deploymanager.restart.dao.RestartDao;
import no.difi.deploymanager.versioncheck.dao.CheckVersionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Service
public class RestartService {
    private final RestartCommandLine restartCommandline;
    private final RestartDao restartDao;
    private final CheckVersionDao checkVersionDao;

    private final List<Status> statuses = new ArrayList<>();

    @Autowired
    public RestartService(RestartDao restartDao, RestartCommandLine restartCommandline, CheckVersionDao checkVersionDao) {
        this.restartDao = restartDao;
        this.restartCommandline = restartCommandline;
        this.checkVersionDao = checkVersionDao;
    }

    public List<Status> execute() {
        try {
            ApplicationList restartList = restartDao.retrieveRestartList();
            ApplicationList runningAppList = checkVersionDao.retrieveRunningAppsList();
            ApplicationData oldApp;

            if (restartList != null && restartList.getApplications() != null) {
                for (ApplicationData newApp : restartList.getApplications()) {
                    oldApp = null;
                    if (runningAppList != null) {
                        oldApp = findAppForRestart(runningAppList, newApp);
                    }
                    try {
                        if (oldApp != null) {
                            boolean result = restartCommandline.executeRestart(oldApp, newApp, restartDao.fetchSelfVersion());
                            if (result) {
                                statuses.add(new Status(
                                        StatusCode.SUCCESS,
                                        format("Application %s updated to version %s",
                                                newApp.getArtifactId(), newApp.getActiveVersion())));
                            }
                        }
                        else {
                            boolean result = restartCommandline.startProcess(newApp);
                            if (result) {
                                statuses.add(new Status(
                                        StatusCode.SUCCESS,
                                        String.format("%s with version %s is started.", newApp.getArtifactId(), newApp.getActiveVersion())
                                ));
                            }
                        }
                    } catch (InterruptedException e) {
                        statuses.add(new Status(StatusCode.ERROR, format("Could not execute restart on %s with version %s. Attempting to start up old version (%s).", newApp.getName(), newApp.getActiveVersion(), oldApp.getActiveVersion())));
                        boolean result = restartCommandline.startProcess(oldApp);

                        if (result) {
                            statuses.add(new Status(StatusCode.SUCCESS, format("Restarted old version %s with success.", oldApp.getActiveVersion())));
                        } else {
                            statuses.add(new Status(StatusCode.ERROR, format("Could not start up %s, %s. Application is not running!", oldApp.getName(), oldApp.getActiveVersion())));
                        }
                    }
                }
            }
        } catch (IOException e) {
            statuses.add(new Status(StatusCode.ERROR, "Local error when retrieving list of applications to restart"));
        }
        return statuses;
    }

    public void saveRestartList(ApplicationList restartList) throws IOException {
        restartDao.saveRestartList(restartList);
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
