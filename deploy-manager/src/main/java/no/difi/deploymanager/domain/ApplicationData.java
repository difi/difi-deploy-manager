package no.difi.deploymanager.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplicationData implements Serializable {
    private final String name;
    private final String groupId;
    private final String artifactId;
    private final String activeVersion;
    private final String filename;
    private final String vmOptions;
    private final String environmentVariables;
    private final String mainClass;
    private final String artifactType;
    private List<DownloadedVersion> downloadedVersions;

    public ApplicationData(Builder data) {
        this.name = data.name;
        this.groupId = data.groupId;
        this.artifactId = data.artifactId;
        this.activeVersion = data.activeVersion;
        this.filename = data.filename;
        this.vmOptions = data.vmOptions;
        this.environmentVariables = data.environmentVariables;
        this.mainClass = data.mainClass;
        this.artifactType = data.artifactType;
        this.downloadedVersions = data.downloadedVersions;
    }

    public String getName() {
        return name;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getActiveVersion() {
        return activeVersion;
    }

    public String getFilename() {
        return filename;
    }

    public String getVmOptions() {
        return vmOptions;
    }

    public String getEnvironmentVariables() {
        return environmentVariables;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getArtifactType() {
        return this.artifactType;
    }

    public List<DownloadedVersion> getDownloadedVersions() {
        if (downloadedVersions == null) {
            downloadedVersions = new ArrayList<>();
        }
        return downloadedVersions;
    }

    public Builder openCopy() {
        return new Builder()
                .name(this.name)
                .groupId(this.groupId)
                .artifactId(this.artifactId)
                .activeVersion(this.activeVersion)
                .filename(this.filename)
                .vmOptions(this.vmOptions)
                .environmentVariables(this.environmentVariables)
                .mainClass(this.mainClass)
                .artifactType(this.artifactType)
                .setAllDownloadedVersions(this.downloadedVersions);
    }

    public static class Builder {
        private String name;
        private String groupId;
        private String artifactId;
        private String activeVersion;
        private String filename;
        private String vmOptions;
        private String environmentVariables;
        private String mainClass;
        private String artifactType;
        private List<DownloadedVersion> downloadedVersions;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder activeVersion(String activeVersion) {
            this.activeVersion = activeVersion;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder vmOptions(String vmOptions) {
            this.vmOptions = vmOptions;
            return this;
        }

        public Builder environmentVariables(String environmentVariables) {
            this.environmentVariables = environmentVariables;
            return this;
        }

        public Builder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        public Builder artifactType(String artifactType) {
            this.artifactType = artifactType;
            return this;
        }

        public Builder addDownloadedVersion(DownloadedVersion downloadedVersion) {
            if (this.downloadedVersions == null) {
                this.downloadedVersions = new ArrayList<>();
            }
            this.downloadedVersions.add(downloadedVersion);
            return this;
        }

        public Builder setAllDownloadedVersions(List<DownloadedVersion> downloadedVersionList) {
            this.downloadedVersions = downloadedVersionList;
            return this;
        }

        public ApplicationData build() {
            return new ApplicationData(this);
        }
    }

}
