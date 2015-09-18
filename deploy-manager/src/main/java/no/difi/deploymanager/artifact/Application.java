package no.difi.deploymanager.artifact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext cac = SpringApplication.run(Application.class, args);

        Startup startup = (Startup)cac.getBean("startup");

        startup.runOnStartup();
    }
}
