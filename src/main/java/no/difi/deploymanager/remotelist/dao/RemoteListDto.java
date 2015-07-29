package no.difi.deploymanager.remotelist.dao;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RemoteListDto {
    public ApplicationList getRemoteList() {
        //TODO: Returning mock for application list. Replace when point to retrieve from is decided.

        ApplicationList remoteApplicationList = new ApplicationList();
        List<ApplicationData> applications = remoteApplicationList.getApplications();

        ApplicationData remoteApp1 = new ApplicationData();
        remoteApp1.setName("SpringJDBC");
        remoteApp1.setGroupId("org.springframework");
        remoteApp1.setArtifactId("spring-jdbc");
        applications.add(remoteApp1);

        ApplicationData remoteApp2 = new ApplicationData();
        remoteApp2.setName("difi-deploy-manager");
        remoteApp2.setGroupId("difi-deploy-manager");
        remoteApp2.setArtifactId("no.difi.deploymanager");
        remoteApp2.setActiveVersion("0.9.0-SNAPSHOT");

        return remoteApplicationList;
    }
}
