package no.difi.deploymanager.remotelist.service;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.remotelist.dao.RemoteListDto;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class RemoteListService {
    private final Environment environment;
    private final RemoteListDto remoteListDto;

    @Autowired
    public RemoteListService(Environment environment, RemoteListDto remoteListDto) {
        this.environment = environment;
        this.remoteListDto = remoteListDto;
    }

    public ApplicationList execute() throws RemoteApplicationListException {
        return remoteListDto.getRemoteList();
    }
}
