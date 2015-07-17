package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import util.IOUtil;
import versioncheck.Scheduler;
import versioncheck.dto.CheckVersionDto;
import versioncheck.service.CheckVersionService;

@Component
public class Beans {
    @Autowired Environment enviroment;

    @Bean(name = "checkVersionService")
    public CheckVersionService checkVersionServiceBean() {
        return new CheckVersionService(enviroment, checkVersionDtoBean());
    }

    @Bean(name = "checkVersionDto")
    public CheckVersionDto checkVersionDtoBean() {
        return new CheckVersionDto(enviroment, ioUtilBean());
    }

    @Bean(name = "scheduler")
    public Scheduler schedulerBean() {
        return new Scheduler(checkVersionServiceBean());
    }

    @Bean(name = "ioUtil")
    public IOUtil ioUtilBean() {
        return new IOUtil();
    }
}
