package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import versioncheck.IOUtil;
import versioncheck.checkversion.CheckVersionService;

public class Beans {
    @Autowired Environment enviroment;
    @Autowired IOUtil ioUtil;

    @Bean(name = "checkVersionService")
    public CheckVersionService checkVersionServiceBean() {
        return new CheckVersionService(enviroment, ioUtil);
    }
}
