package no.difi.deploymanager.restart.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.restart.dao.RestartCommandLine;
import no.difi.deploymanager.restart.dao.RestartDao;
import no.difi.deploymanager.versioncheck.dao.CheckVersionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static no.difi.deploymanager.util.StatusFactory.statusError;
import static no.difi.deploymanager.util.StatusFactory.statusSuccess;

@Service
public class RestartService {
    private final RestartCommandLine restartCommandline;
    private final RestartDao restartDao;
    private final CheckVersionDao checkVersionDao;

    private List<Status> statuses = new ArrayList<>();

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

            if (restartList != null && restartList.getApplications() != null) {
                for (ApplicationData newApp : restartList.getApplications()) {
                    ApplicationData oldApp = null;
                    if (runningAppList != null) {
                        oldApp = findAppForRestart(runningAppList, newApp);
                    }
                    if (oldApp != null) {
                        statuses.add(performRestart(newApp, oldApp));
                    } else {
                        statuses.add(performStart(newApp));
                    }
                }
            }
        } catch (IOException e) {
            statuses.add(statusError("Local error when retrieving list of applications to restart"));
        }
        return statuses;
    }

    private Status performStart(ApplicationData newApp) {
        boolean result = restartCommandline.startProcess(newApp);
        if (result) {
            return statusSuccess(format("%s with version %s is started.", newApp.getArtifactId(), newApp.getActiveVersion()));
        }
        else {
            return statusError(String.format("Failed restart of %s", newApp.getName()));
        }
    }

    private Status performRestart(ApplicationData newApp, ApplicationData oldApp) throws IOException {
        try {
            boolean result = restartCommandline.executeRestart(oldApp, newApp, restartDao.fetchSelfVersion());
            if (result) {
                return statusSuccess(format("Application %s updated to version %s", newApp.getArtifactId(), newApp.getActiveVersion()));
            }
        } catch (InterruptedException e) {
            return failoverRestartOldVersion(oldApp);
        }
        return statusError(String.format("Failed start of %s", newApp.getName()));
    }

    private Status failoverRestartOldVersion(ApplicationData oldApp) {
        boolean result = restartCommandline.startProcess(oldApp);

        if (result) {
            return statusSuccess(format("Restarted old version %s with success.", oldApp.getActiveVersion()));
        } else {
            return statusError(format("Failed start of old version as failover. Application %s is not running!", oldApp.getName()));
        }
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
