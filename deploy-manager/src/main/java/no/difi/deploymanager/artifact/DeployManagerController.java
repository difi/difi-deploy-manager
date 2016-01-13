package no.difi.deploymanager.artifact;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

/***
 * Environment spesific properties
 */
@Component
@PropertySources(value = {
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:application-${spring.profiles.active}.properties"),
})
public class DeployManagerController {

}
