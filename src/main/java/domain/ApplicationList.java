package domain;

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
}
