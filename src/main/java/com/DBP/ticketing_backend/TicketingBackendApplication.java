package com.DBP.ticketing_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class TicketingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingBackendApplication.class, args);
    }
}
