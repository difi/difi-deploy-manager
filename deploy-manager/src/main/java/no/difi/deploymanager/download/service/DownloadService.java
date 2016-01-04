package no.difi.deploymanager.download.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.DownloadedVersion;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.download.filetransfer.FileTransfer;
import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static no.difi.deploymanager.util.StatusFactory.statusError;
import static no.difi.deploymanager.util.StatusFactory.statusSuccess;

/***
 * DownloadService contains logic for downloading application, and logs result of steps in the process.
 */
public class DownloadService {
    private final DownloadDao downloadDao;
    private final FileTransfer fileTransfer;
    private final RestartService restartService;

    private final List<Status> statuses = new ArrayList<>();

    public DownloadService(DownloadDao downloadDao, FileTransfer fileTransfer, RestartService restartService) {
        this.downloadDao = downloadDao;
        this.fileTransfer = fileTransfer;
        this.restartService = restartService;
    }

    public List<Status> execute() {
        ApplicationList forDownload = new ApplicationList.Builder().build();
        try {
            forDownload = retrieveDownloadList();
        } catch (IOException e) {
            statuses.add(statusError("Failed to retrieve download list."));
        }

        if (forDownload != null && forDownload.getApplications() != null) {
            ApplicationList restartList = null;
            try {
                restartList = downloadApplications(forDownload);
            } catch (IOException e) {
                statuses.add(statusError("Failed to download applications."));
            }

            // TODO: fix FindBugs problem: restartList can be null when it should not
            ApplicationList notDownloaded = updateNotDownloadedList(restartList, forDownload);
            try {
                downloadDao.saveDownloadList(notDownloaded);
            } catch (IOException e) {
                statuses.add(statusError("Failed to save download list."));
            }

            try {
                saveRestartList(restartList);
            } catch (IOException e) {
                statuses.add(statusError("Failed to save restart list."));
            }
        } else {
            statuses.add(statusSuccess("Nothing to download."));
        }

        return statuses;
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
            statuses.add(statusSuccess("Downloaded apps, prepared for restart."));
        } else {
            statuses.add(statusSuccess("No applications set for download."));
        }
    }

    private ApplicationList downloadApplications(ApplicationList forDownload) throws IOException {
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
                statuses.add(statusError(format("Failed to compose URL for %s.", data.getName())));
            } catch (SocketTimeoutException e) {
                statuses.add(statusError(format("Timeout occurred. It too too long to download %s %s", data.getName(), data.getFilename())));
            } catch (ConnectionFailedException e) {
                statuses.add(statusError("Connection for downloading updates failed."));
            }
        }

        return restartList.build();
    }
}
