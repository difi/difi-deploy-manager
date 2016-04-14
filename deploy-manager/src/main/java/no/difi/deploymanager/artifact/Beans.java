package no.difi.deploymanager.artifact;

import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.download.filetransfer.FileTransfer;
import no.difi.deploymanager.download.service.DownloadService;
import no.difi.deploymanager.remotelist.dao.ApplicationListRepository;
import no.difi.deploymanager.remotelist.service.ApplicationListService;
import no.difi.deploymanager.restart.dao.RestartCommandLine;
import no.difi.deploymanager.restart.dao.RestartDao;
import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.schedule.Scheduler;
import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.util.JsonUtil;
import no.difi.deploymanager.versioncheck.dao.CheckVersionDao;
import no.difi.deploymanager.versioncheck.service.CheckVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@SuppressWarnings("SpringFacetCodeInspection")
public class Beans {
    @Autowired Environment environment;

    @Bean
    public CheckVersionService checkVersionService() {
        return new CheckVersionService(remoteListService(), checkVersionDao(), downloadDao());
    }

    @Bean
    public CheckVersionDao checkVersionDao() {
        return new CheckVersionDao(environment, ioUtil(), jsonUtil());
    }

    @Bean
    public DownloadService downloadService() {
        return new DownloadService(downloadDao(), fileTransfer(), restartService());
    }

    @Bean
    public DownloadDao downloadDao() {
        return new DownloadDao(environment, ioUtil());
    }

    @Bean
    public RestartService restartService() {
        return new RestartService(restartDao(), restartCommandLine(), checkVersionService());
    }

    @Bean
    public RestartDao restartDao() {
        return new RestartDao(environment, ioUtil());
    }

    @Bean
    public RestartCommandLine restartCommandLine() {
        return new RestartCommandLine(environment);
    }

    @Bean
    public ApplicationListService remoteListService() {
        return new ApplicationListService(remoteListRepository());
    }

    @Bean
    public ApplicationListRepository remoteListRepository() {
        return new ApplicationListRepository(jsonUtil());
    }

    @Bean
    public Scheduler scheduler() {
        return new Scheduler(checkVersionService(), downloadService(), restartService());
    }

    @Bean
    public FileTransfer fileTransfer() {
        return new FileTransfer(environment);
    }

    @Bean
    public IOUtil ioUtil() {
        return new IOUtil();
    }

    @Bean
    public JsonUtil jsonUtil() {
        return new JsonUtil();
    }

    @Bean
    public Startup startup() {
        return new Startup(checkVersionService(), downloadService(), restartService());
    }
}
