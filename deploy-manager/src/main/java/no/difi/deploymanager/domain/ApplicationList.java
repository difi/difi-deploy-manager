package no.difi.deploymanager.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplicationList implements Serializable {
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

    public boolean hasApplicationData(ApplicationData data) {
        for (ApplicationData inList : applications) {
            if (inList.getName().equals(data.getName())
                    && inList.getGroupId().equals(data.getGroupId())
                    && inList.getArtifactId().equals(data.getArtifactId())) {
                return true;
            }
        }
        return false;
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
