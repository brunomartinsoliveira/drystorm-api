package com.drystorm.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DryStormApplication {

    public static void main(String[] args) {
        SpringApplication.run(DryStormApplication.class, args);
    }
}
