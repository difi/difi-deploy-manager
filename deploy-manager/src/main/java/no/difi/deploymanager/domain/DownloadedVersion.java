package no.difi.deploymanager.domain;

import java.io.Serializable;

public class DownloadedVersion implements Serializable {
    private final String version;

    @SuppressWarnings("WeakerAccess")
    public DownloadedVersion(Builder downloaded) {
        this.version = downloaded.version;
    }

    public String getVersion() {
        return version;
    }

    public static class Builder {
        private String version;

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public DownloadedVersion build() {
            return new DownloadedVersion(this);
        }
    }
}
