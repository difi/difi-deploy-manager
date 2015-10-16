package no.difi.deploymanager.remotelist.service;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.remotelist.dao.RemoteListRepository;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;

import java.io.IOException;

public class RemoteListService {
    private final RemoteListRepository remoteListRepository;

    public RemoteListService(RemoteListRepository remoteListRepository) {
        this.remoteListRepository = remoteListRepository;
    }

    public ApplicationList execute() throws RemoteApplicationListException, IOException {
        //RemoteApplicationListException is thrown from remote list. Gets warning when hardcoded list is active.
//        return remoteListRepository.getRemoteList();
        return remoteListRepository.getLocalList();
    }
}
