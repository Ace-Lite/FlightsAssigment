package com.czechpostassigment.flightsviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FlightDepartApp {
    public static void main(String[] args) {
        SpringApplication.run(FlightDepartApp.class, args);
    }
}
