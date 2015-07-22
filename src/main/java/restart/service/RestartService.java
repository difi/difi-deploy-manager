package restart.service;

import domain.ApplicationData;
import domain.ApplicationList;
import domain.Status;
import domain.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import restart.dto.RestartDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RestartService {
    private final RestartDto restartDto;

    private List<Status> statuses = new ArrayList<>();

    @Autowired
    public RestartService(RestartDto restartDto) {
        this.restartDto = restartDto;
    }

    public List<Status> execute() {
        try {
            ApplicationList restartList = restartDto.retrieveRestartList();

            if (restartList != null && restartList.getApplications() != null) {
                for (ApplicationData data : restartList.getApplications()) {
                    restartDto.executeRestart(data);
                }
            }

        } catch (IOException e) {
            statuses.add(new Status(StatusCode.ERROR, "Local error when retrieving list of applications to restart"));
        }
        return statuses;
    }
}
