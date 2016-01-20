package no.difi.deploymanager.artifact;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

/***
 * Environment spesific properties
 *
 * Profiles can be set by using -spring.profiles.active=[profil-name]
 * Valid profiles are (with intent):
 *      dev         Local development
 *      itest       Integration test server
 *      systest     System test server
 *      staging     Staging server (with build version increase)
 *      production  Production for customer's to use
 */
@Component
@PropertySources(value = {
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:application-${spring.profiles.active}.properties"),
})
public class DeployManagerController {

}
