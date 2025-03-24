package dev.kapiaszczyk.mpp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@OpenAPIDefinition(info = @Info(title = "MPP API", version = "1.0", description = "API for managing photos and albums"))
public class MppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MppApplication.class, args);
    }

}
