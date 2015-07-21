package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplicationData implements Serializable {
    MonitoringApplications name;
    String activeVersion;
    List<DownloadedVersion> downloadedVersions;

    public MonitoringApplications getName() {
        return name;
    }

    public void setName(MonitoringApplications name) {
        this.name = name;
    }

    public String getActiveVersion() {
        return activeVersion;
    }

    public void setActiveVersion(String activeVersion) {
        this.activeVersion = activeVersion;
    }

    public List<DownloadedVersion> getDownloadedVersions() {
        if (downloadedVersions == null) {
            downloadedVersions = new ArrayList<>();
        }
        return downloadedVersions;
    }

    public void setDownloadedVersions(List<DownloadedVersion> downloadedVersions) {
        this.downloadedVersions = downloadedVersions;
    }
}
