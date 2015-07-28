package no.difi.deploymanager.remotelist.dao;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import org.springframework.stereotype.Repository;

@Repository
public class RemoteListDto {
    public ApplicationList getRemoteList() {
        //TODO: Returning mock for application list. Replace when point to retrieve from is decided.
        ApplicationData remoteApp = new ApplicationData();
        remoteApp.setName("SpringJDBC");
        remoteApp.setGroupId("org.springframework");
        remoteApp.setArtifactId("spring-jdbc");

        return new ApplicationList();
    }
}
