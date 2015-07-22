package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        System.out.println("***********************************");
        System.out.println("java.runtime.name: " + System.getProperty("java.runtime.name"));
        System.out.println("java.protocol.handler.pkgs: " + System.getProperty("java.protocol.handler.pkgs"));
        System.out.println("java.vm.version: " + System.getProperty("java.vm.version"));
        System.out.println("user.country.format: " + System.getProperty("user.country.format"));

        System.out.println("Test-application is up and running.");
        System.out.println("***********************************");
    }
}
