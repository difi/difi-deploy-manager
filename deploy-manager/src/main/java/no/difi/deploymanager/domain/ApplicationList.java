package no.difi.deploymanager.domain;

import java.io.Serializable;
import java.util.List;

public class ApplicationList implements Serializable {
    private List<ApplicationData> applications;

    public List<ApplicationData> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationData> application) {
        this.applications = application;
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
}
