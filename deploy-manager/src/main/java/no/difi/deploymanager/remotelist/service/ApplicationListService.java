package no.difi.deploymanager.remotelist.service;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.remotelist.dao.ApplicationListRepository;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;

import java.io.IOException;

public class ApplicationListService {
    private final ApplicationListRepository applicationListRepository;

    public ApplicationListService(ApplicationListRepository applicationListRepository) {
        this.applicationListRepository = applicationListRepository;
    }

    public ApplicationList execute() throws RemoteApplicationListException, IOException {
        //RemoteApplicationListException is thrown from remote list. Gets warning when hardcoded list is active.
        return applicationListRepository.getLocalList();
    }
}
