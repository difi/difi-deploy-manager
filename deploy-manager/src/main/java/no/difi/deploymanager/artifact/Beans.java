package no.difi.deploymanager.artifact;

import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.download.filetransfer.FileTransfer;
import no.difi.deploymanager.download.service.DownloadService;
import no.difi.deploymanager.remotelist.dao.RemoteListRepository;
import no.difi.deploymanager.remotelist.service.RemoteListService;
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
    @Autowired Environment enviroment;

    @Bean(name = "checkVersionService")
    public CheckVersionService checkVersionServiceBean() {
        return new CheckVersionService(remoteListServiceBean(), checkVersionDaoBean(), downloadDaoBean());
    }

    @Bean(name = "checkVersionDao")
    public CheckVersionDao checkVersionDaoBean() {
        return new CheckVersionDao(enviroment, ioUtilBean(), jsonUtilBean());
    }

    @Bean(name = "downloadService")
    public DownloadService downloadServiceBean() {
        return new DownloadService(downloadDaoBean(), fileTransferBean(), restartServiceBean());
    }

    @Bean(name = "downloadDao")
    public DownloadDao downloadDaoBean() {
        return new DownloadDao(enviroment, ioUtilBean());
    }

    @Bean(name = "restartService")
    public RestartService restartServiceBean() {
        return new RestartService(restartDaoBean(), restartCommandLineBean(), checkVersionServiceBean());
    }

    @Bean(name = "restartDao")
    public RestartDao restartDaoBean() {
        return new RestartDao(enviroment, ioUtilBean());
    }

    @Bean(name = "restartCommandLine")
    public RestartCommandLine restartCommandLineBean() {
        return new RestartCommandLine(enviroment);
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

    @Bean(name = "fileTransfer")
    public FileTransfer fileTransferBean() {
        return new FileTransfer(enviroment);
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
