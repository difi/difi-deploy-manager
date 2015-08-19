package no.difi.deploymanager.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplicationData implements Serializable {
    private String name;
    private String groupId;
    private String artifactId;
    private String activeVersion;
    private String filename;
    private String startParameters;
    private String artifactType;
    private List<DownloadedVersion> downloadedVersions;

    public ApplicationData(Builder data) {
        this.name = data.name;
        this.groupId = data.groupId;
        this.artifactId = data.artifactId;
        this.activeVersion = data.activeVersion;
        this.filename = data.filename;
        this.startParameters = data.startParameters;
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

    public Builder openCopy() {
        return new Builder()
                .name(this.name)
                .groupId(this.groupId)
                .artifactId(this.artifactId)
                .activeVersion(this.activeVersion)
                .filename(this.filename)
                .startParameters(this.startParameters)
                .artifactType(this.artifactType)
                .setAllDownloadedVersion(this.downloadedVersions);
    }

    public List<DownloadedVersion> getDownloadedVersions() {
        if (downloadedVersions == null) {
            downloadedVersions = new ArrayList<>();
        }
        return downloadedVersions;
    }

    public String getStartParameters() {
        return startParameters;
    }

    public String getArtifactType() {
        return this.artifactType;
    }

    public static class Builder {
        private String name;
        private String groupId;
        private String artifactId;
        private String activeVersion;
        private String filename;
        private String startParameters;
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

        public Builder startParameters(String startParameters) {
            this.startParameters = startParameters;
            return this;
        }

        public Builder artifactType(String artifactType) {
            this.artifactType = artifactType;
            return this;
        }

        public Builder addDownloadedVersions(DownloadedVersion downloadedVerision) {
            if (this.downloadedVersions == null) {
                this.downloadedVersions = new ArrayList<>();
            }
            this.downloadedVersions.add(downloadedVerision);
            return this;
        }

        public Builder setAllDownloadedVersion(List<DownloadedVersion> downloadedVersionList) {
            this.downloadedVersions = downloadedVersionList;
            return this;
        }

        public ApplicationData build() {
            return new ApplicationData(this);
        }
    }

}
