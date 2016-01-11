package no.difi.deploymanager.restart.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.restart.dao.RestartCommandLine;
import no.difi.deploymanager.restart.dao.RestartDao;
import no.difi.deploymanager.versioncheck.service.CheckVersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

/***
 * Restart service contains business logic and error handling for starting, stopping and restarting applications.
 */
public class RestartService {
    private static final Logger logger = LoggerFactory.getLogger(RestartService.class);

    private final RestartCommandLine restartCommandline;
    private final RestartDao restartDao;
    private final CheckVersionService checkVersionService;

    public RestartService(RestartDao restartDao, RestartCommandLine restartCommandline, CheckVersionService checkVersionService) {
        this.restartDao = restartDao;
        this.restartCommandline = restartCommandline;
        this.checkVersionService = checkVersionService;
    }

    public void execute() {
        ApplicationList restartList = retrieveRestartList();
        ApplicationList runningAppList = retrieveRunningAppListWithStatus();

        List<Integer> removeFromRestartIndex = new ArrayList<>(2);
        if (restartList != null && restartList.getApplications() != null) {
            for (ApplicationData app : restartList.getApplications()) {
                ApplicationData appWithNewVersion = null;
                if (runningAppList != null) {
                    appWithNewVersion = findAppWithNewVersion(runningAppList, app);
                }
                if (appWithNewVersion != null) {
                    runningAppList = restartProcess(runningAppList, app, appWithNewVersion);
                } else {
                    runningAppList = startProcess(restartList, runningAppList, removeFromRestartIndex, app);
                }
            }
            restartList = cleanRestartList(restartList, removeFromRestartIndex);
        }
        saveRestartList(restartList);
        saveRunningAppsAndVerifyRunningStatus(runningAppList);
    }

    private ApplicationList restartProcess(ApplicationList runningAppList, ApplicationData app, ApplicationData appWithNewVersion) {
        try {
            restartApplicationInProcessWithNewVersion(app, appWithNewVersion);
            int index = runningAppList.getApplications().indexOf(appWithNewVersion);
            runningAppList.getApplications().remove(appWithNewVersion);
            runningAppList.getApplications().add(index, app);
        } catch (IOException e) {
            logger.info("Nothing to perform restart on");
        }
        return runningAppList;
    }

    private ApplicationList startProcess(ApplicationList restartList, ApplicationList runningAppList,
                                         List<Integer> removeFromRestartIndex, ApplicationData app) {
        startApplicationInProcess(app);
        if (runningAppList == null) {
            runningAppList = new ApplicationList.Builder().build();
        }
        runningAppList.getApplications().add(app);
        removeFromRestartIndex.add(restartList.getApplications().indexOf(app));

        return runningAppList;
    }

    private ApplicationList cleanRestartList(ApplicationList restartList, List<Integer> removeFromRestartIndex) {
        Collections.reverse(removeFromRestartIndex);
        for (int index : removeFromRestartIndex) {
            restartList.getApplications().remove(index);
        }
        return restartList;
    }

    private ApplicationList retrieveRunningAppListWithStatus() {
        try {
            return checkVersionService.retrieveRunningAppsList();
        } catch (IOException e) {
            logger.error("Retrieve running app list not found");
        }
        return null;
    }

    private ApplicationList retrieveRestartList() {
        try {
            return restartDao.retrieveRestartList();
        } catch (IOException e) {
            logger.error("Restart list not found");
        }
        return null;
    }

    private ApplicationData findAppWithNewVersion(ApplicationList runningAppList, ApplicationData newApp) {
        ApplicationData appWithNewVersion = null;
        for (ApplicationData oldApp : runningAppList.getApplications()) {
            if (newApp.getGroupId().equals(oldApp.getGroupId()) && newApp.getArtifactId().equals(oldApp.getArtifactId())) {
                appWithNewVersion = oldApp;
                break;
            }
        }
        return appWithNewVersion;
    }

    private void startApplicationInProcess(ApplicationData newApp) {
        boolean result = restartCommandline.startProcess(newApp);
        if (result) {
            logger.info(format("%s with version %s is started.", newApp.getArtifactId(), newApp.getActiveVersion()));
        }
        else {
            logger.error(format("Failed restart of %s", newApp.getName()));
        }
    }

    private void restartApplicationInProcessWithNewVersion(ApplicationData newApp, ApplicationData oldApp) throws IOException {
        boolean result = restartCommandline.executeRestart(oldApp, newApp, restartDao.fetchSelfVersion());
        if (result) {
            logger.info(format("Application %s updated to version %s", newApp.getArtifactId(), newApp.getActiveVersion()));
        }
        logger.error(format("Failed start of %s", newApp.getName()));
    }

    public void performSaveOfRestartList(ApplicationList restartList) throws IOException {
        restartDao.saveRestartList(restartList);
    }

    private void saveRestartList(ApplicationList restartList) {
        try {
            performSaveOfRestartList(restartList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveRunningAppsAndVerifyRunningStatus(ApplicationList runningAppList) {
        if (runningAppList != null) {
            try {
                checkVersionService.saveRunningAppsList(runningAppList);

                verifyThatApplicationsAreRunning(runningAppList);
            } catch (IOException e) {
                logger.info("Currently I have no running apps");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void verifyThatApplicationsAreRunning(ApplicationList runningAppList) throws IOException, InterruptedException {
        for (ApplicationData data : runningAppList.getApplications()) {
            if (restartCommandline.findProcessId(data).length() == 0) {
                restartCommandline.startProcess(data);
            }
        }
    }
}
