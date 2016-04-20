package no.difi.deploymanager.domain;

import no.difi.deploymanager.versioncheck.dao.MavenArtifact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ApplicationList implements Serializable, Iterable<ApplicationData> {
    private List<ApplicationData> applications;

    public ApplicationList(Builder builder) {
        this.applications = builder.dataList;
    }

    public List<ApplicationData> getApplications() {
        if (applications == null) {
            applications = new ArrayList<>();
        }
        return applications;
    }

    public boolean containsMavenArtifact(final MavenArtifact artificat) {
        for(ApplicationData data : applications) {
            if(data.isMavenArtifact(artificat)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasApplicationData(ApplicationData data) {
        for (ApplicationData inList : applications) {
            if (inList.getName().equals(data.getName()) &&
                    inList.isMavenArtifact(data.getMavenArtifact())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<ApplicationData> iterator() {
        return applications.iterator();
    }

    public static class Builder {
        private List<ApplicationData> dataList;

        public Builder addApplicationData(ApplicationData applicationData) {
            if (dataList == null) {
                dataList = new ArrayList<>();
            }
            dataList.add(applicationData);
            return this;
        }

        public ApplicationList build() {
            return new ApplicationList(this);
        }
    }
}
