package no.difi.deploymanager.domain;

public class DownloadedVersion {
    private String version;

    public DownloadedVersion(Builder downloaded) {
        this.version = downloaded.version;
    }

    public void setVersion(String version) {
        this.version = version;
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
