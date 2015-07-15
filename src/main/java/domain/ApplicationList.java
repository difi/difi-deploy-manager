package domain;

import java.util.List;

public class ApplicationList {
    private List<ApplicationData> applications;

    public List<ApplicationData> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationData> application) {
        this.applications = application;
    }
}
