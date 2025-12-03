package com.teamvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@ComponentScan(basePackages = {"com.teamvault"})
@EnableMongoAuditing
public class TeamVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamVaultApplication.class, args);
    }

}
