package application;

import download.dto.DownloadDto;
import download.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import restart.dto.RestartDto;
import restart.service.RestartService;
import schedule.Scheduler;
import util.IOUtil;
import versioncheck.dto.CheckVersionDto;
import versioncheck.service.CheckVersionService;

@Component
public class Beans {
    @Autowired Environment enviroment;

    @Bean(name = "checkVersionService")
    public CheckVersionService checkVersionServiceBean() {
        return new CheckVersionService(enviroment, checkVersionDtoBean(), downloadDtoBean());
    }

    @Bean(name = "checkVersionDto")
    public CheckVersionDto checkVersionDtoBean() {
        return new CheckVersionDto(enviroment, ioUtilBean());
    }

    @Bean(name = "downloadService")
    public DownloadService downloadServiceBean() {
        return new DownloadService(enviroment, downloadDtoBean(), restartDtoBean());
    }

    @Bean(name = "downloadDto")
    private DownloadDto downloadDtoBean() {
        return new DownloadDto(enviroment, ioUtilBean());
    }

    @Bean(name = "restartService")
    public RestartService restartServiceBean() {
        return new RestartService(restartDtoBean());
    }

    @Bean(name = "restartDto")
    public RestartDto restartDtoBean() {
        return new RestartDto(enviroment, ioUtilBean());
    }

    @Bean(name = "scheduler")
    public Scheduler schedulerBean() {
        return new Scheduler(checkVersionServiceBean(), downloadServiceBean(), restartServiceBean());
    }

    @Bean(name = "ioUtil")
    public IOUtil ioUtilBean() {
        return new IOUtil();
    }
}
