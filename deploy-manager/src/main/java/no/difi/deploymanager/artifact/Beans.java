package no.difi.deploymanager.artifact;

import no.difi.deploymanager.download.dto.DownloadDto;
import no.difi.deploymanager.download.service.DownloadService;
import no.difi.deploymanager.remotelist.dao.RemoteListRepository;
import no.difi.deploymanager.remotelist.service.RemoteListService;
import no.difi.deploymanager.restart.dto.RestartDto;
import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.schedule.Scheduler;
import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.util.JsonUtil;
import no.difi.deploymanager.versioncheck.repository.CheckVersionRepository;
import no.difi.deploymanager.versioncheck.service.CheckVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class Beans {
    @Autowired Environment enviroment;

    @Bean(name = "checkVersionService")
    public CheckVersionService checkVersionServiceBean() {
        return new CheckVersionService(remoteListServiceBean(), checkVersionRepositoryBean(), downloadDtoBean());
    }

    @Bean(name = "checkVersionRepository")
    public CheckVersionRepository checkVersionRepositoryBean() {
        return new CheckVersionRepository(enviroment, ioUtilBean(), jsonUtilBean());
    }

    @Bean(name = "downloadService")
    public DownloadService downloadServiceBean() {
        return new DownloadService(enviroment, downloadDtoBean(), restartDtoBean());
    }

    @Bean(name = "downloadDto")
    public DownloadDto downloadDtoBean() {
        return new DownloadDto(enviroment, ioUtilBean());
    }

    @Bean(name = "restartService")
    public RestartService restartServiceBean() {
        return new RestartService(restartDtoBean(), checkVersionRepositoryBean());
    }

    @Bean(name = "restartDto")
    public RestartDto restartDtoBean() {
        return new RestartDto(enviroment, ioUtilBean());
    }

    @Bean(name = "remoteListService")
    public RemoteListService remoteListServiceBean() {
        return new RemoteListService(remoteListRepositoryBean());
    }

    @Bean(name = "remoteListRepository")
    public RemoteListRepository remoteListRepositoryBean() {
        return new RemoteListRepository(jsonUtilBean());
    }

    @Bean(name = "scheduler")
    public Scheduler schedulerBean() {
        return new Scheduler(checkVersionServiceBean(), downloadServiceBean(), restartServiceBean());
    }

    @Bean(name = "ioUtil")
    public IOUtil ioUtilBean() {
        return new IOUtil();
    }

    @Bean(name = "jsonUtil")
    public JsonUtil jsonUtilBean() {
        return new JsonUtil();
    }
}
