package no.difi.deploymanager.download.service;

import no.difi.deploymanager.download.dto.DownloadDto;
import no.difi.deploymanager.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import no.difi.deploymanager.restart.dto.RestartDto;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Service
public class DownloadService {
    private final Environment environment;
    private final DownloadDto downloadDto;
    private final RestartDto restartDto;

    private List<Status> statuses = new ArrayList<>();

    @Autowired
    public DownloadService(Environment environment, DownloadDto downloadDto, RestartDto restartDto) {
        this.environment = environment;
        this.downloadDto = downloadDto;
        this.restartDto = restartDto;
    }

    public List<Status> execute() {
        String url;
        List<ApplicationData> restartList;

        try {
            url = environment.getRequiredProperty("location.download");
        } catch (IllegalStateException e) {
            statuses.add(new Status(StatusCode.CRITICAL, "Enviroment property 'location.version' not found."));
            return statuses;
        }

        try {
            ApplicationList forDownload = downloadDto.retrieveDownloadList();

            if (forDownload.getApplications() != null) {
                restartList = downloadApplications(url, forDownload);

                ApplicationList notDownloaded = new ApplicationList();
                notDownloaded.setApplications(updateNotDownloadedList(restartList, forDownload));
                downloadDto.saveDownloadList(notDownloaded);

                saveRestartList(restartList);
            } else {
                statuses.add(new Status(StatusCode.SUCCESS, "Nothing to download."));
            }
        }
        catch (IOException e) {
            statuses.add(new Status(StatusCode.ERROR, format("Failed to retrieve download list. Reason: %s", e.getMessage())));
        }

        return statuses;
    }

    private List<ApplicationData> updateNotDownloadedList(List<ApplicationData> restartList, ApplicationList forDownload) {
        List<ApplicationData> notDownloaded = new ArrayList<>();

        //Update list for applications that failed download.
        for (ApplicationData checklist : restartList) {
            boolean found = false;
            for (ApplicationData worklist : forDownload.getApplications()) {
                if (checklist.getName() == worklist.getName()) {
                    found = true;
                }
            }
            if (!found) {
                notDownloaded.add(checklist);
            }
        }
        return notDownloaded;
    }

    private void saveRestartList(List<ApplicationData> restartList) throws IOException {
        if (restartList != null && restartList.size() != 0) {
            ApplicationList applicationsForRestart = new ApplicationList();
            applicationsForRestart.setApplications(restartList);

            restartDto.saveRestartList(applicationsForRestart);

            statuses.add(new Status(StatusCode.SUCCESS, format("Downloaded apps, prepared for restart.")));
        }
        else {
            statuses.add(new Status(StatusCode.SUCCESS, format("No applications set for download.")));
        }
    }

    private List<ApplicationData> downloadApplications(String url, ApplicationList forDownload) throws IOException {
        List<ApplicationData> restartList = new ArrayList<>();

        for (ApplicationData data : forDownload.getApplications()) {
            try {
                String versionDownloaded = downloadDto.downloadApplication(url);

                DownloadedVersion downloadedVersion = new DownloadedVersion();
                downloadedVersion.setVersion(versionDownloaded);

                List<DownloadedVersion> allVersions = data.getDownloadedVersions();
                allVersions.add(downloadedVersion);
                data.setDownloadedVersions(allVersions);

                restartList.add(data);
            }
            catch (MalformedURLException e) {
                statuses.add(new Status(StatusCode.ERROR,
                        format("Failed to compose url: %s Reason: %s", url, e.getMessage())));
            }
            catch (SocketTimeoutException e) {
                statuses.add(new Status(StatusCode.ERROR,
                        format("Timeout occured. Took too long to download from %s Reason: %s", url, e.getMessage())));
            }
            catch (ConnectionFailedException e) {
                statuses.add(new Status(StatusCode.ERROR, format("Failed to retrieve data from %s Reason: %s", url, e.getMessage())));
            }
        }

        return restartList;
    }
}
