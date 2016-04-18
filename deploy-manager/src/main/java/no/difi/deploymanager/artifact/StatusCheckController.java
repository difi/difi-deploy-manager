package no.difi.deploymanager.artifact;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.remotelist.service.ApplicationListService;
import no.difi.deploymanager.versioncheck.service.CheckVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class StatusCheckController {
    private final CheckVersionService checkVersionService;
    private final ApplicationListService applicationListService;

    @Autowired
    public StatusCheckController(CheckVersionService checkVersionService, ApplicationListService applicationListService) {
        this.checkVersionService = checkVersionService;
        this.applicationListService = applicationListService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/running")
    public @ResponseBody ApplicationList checkVersion() {
        try {
            return checkVersionService.retrieveRunningAppsList();
        } catch (IOException e) {
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/remotelist")
    public @ResponseBody
    ApplicationList remoteList() {
        try {
            return applicationListService.getApplications();
        } catch (RemoteApplicationListException | IOException e) {
            return null;
        }
    }
}
