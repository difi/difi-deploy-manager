package no.difi.deploymanager.domain;

public class Self {
    private final String name;
    private final String version;

    public Self(Builder self) {
        this.name = self.name;
        this.version = self.version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public static class Builder {
        private String name;
        private String version;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Self build() {
            return new Self(this);
        }
    }
}
