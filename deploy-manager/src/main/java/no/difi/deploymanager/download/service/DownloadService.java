package no.difi.deploymanager.download.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.DownloadedVersion;
import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.download.filetransfer.FileTransfer;
import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import static java.lang.String.format;

/***
 * DownloadService contains logic for downloading application, and logs result of steps in the process.
 */
public class DownloadService {
    private static final Logger logger = LogManager.getLogger(DownloadService.class);

    private final DownloadDao downloadDao;
    private final FileTransfer fileTransfer;
    private final RestartService restartService;

    public DownloadService(DownloadDao downloadDao, FileTransfer fileTransfer, RestartService restartService) {
        this.downloadDao = downloadDao;
        this.fileTransfer = fileTransfer;
        this.restartService = restartService;
    }

    public void execute() {
        ApplicationList forDownload = new ApplicationList.Builder().build();
        try {
            forDownload = retrieveDownloadList();
        } catch (IOException e) {
            logger.log(Level.ERROR, "Failed to retrieve download list.");
        }

        if (forDownload != null && forDownload.getApplications() != null) {
            ApplicationList restartList = downloadApplications(forDownload);

            ApplicationList notDownloaded = updateNotDownloadedList(restartList, forDownload);
            try {
                downloadDao.saveDownloadList(notDownloaded);
            } catch (IOException e) {
                logger.log(Level.ERROR, "Failed to save download list.");
            }

            try {
                saveRestartList(restartList);
            } catch (IOException e) {
                logger.log(Level.ERROR, "Failed to save restart list.");
            }
        } else {
            logger.log(Level.INFO, "Nothing to download.");
        }
    }

    public ApplicationList retrieveDownloadList() throws IOException {
        return downloadDao.retrieveDownloadList();
    }

    private ApplicationList updateNotDownloadedList(ApplicationList restartList, ApplicationList forDownload) {
        ApplicationList.Builder appList = new ApplicationList.Builder();

        //Update list for applications that failed download.
        for (ApplicationData checklist : restartList.getApplications()) {
            if (!forDownload.hasApplicationData(checklist)) {
                appList.addApplicationData(checklist);
            }
        }

        return appList.build();
    }

    private void saveRestartList(ApplicationList restartList) throws IOException {
        if (restartList != null && restartList.getApplications().size() != 0) {
            restartService.performSaveOfRestartList(restartList);
            logger.log(Level.INFO, "Downloaded apps, prepared for restart.");
        } else {
            logger.log(Level.INFO, "No applications set for download.");
        }
    }

    private ApplicationList downloadApplications(ApplicationList forDownload) {
        ApplicationList.Builder restartList = new ApplicationList.Builder();

        for (ApplicationData data : forDownload.getApplications()) {
            try {
                String filenameDownloaded = fileTransfer.downloadApplication(data);

                ApplicationData.Builder appData = data.openCopy();
                appData.setAllDownloadedVersions(data.getDownloadedVersions())
                        .filename(filenameDownloaded)
                        .addDownloadedVersion(new DownloadedVersion.Builder().version(data.getActiveVersion()).build());

                restartList.addApplicationData(appData.build());
            } catch (MalformedURLException e) {
                logger.log(Level.ERROR, format("Failed to compose URL for %s.", data.getName()));
            } catch (SocketTimeoutException e) {
                logger.log(Level.ERROR, format("Timeout occurred. It too too long to download %s %s", data.getName(), data.getFilename()));
            } catch (ConnectionFailedException e) {
                logger.log(Level.ERROR, "Connection for downloading updates failed.");
            } catch (IOException e) {
                logger.log(Level.ERROR, "Failed to download applications.");
            }
        }

        return restartList.build();
    }
}
