package com.vive.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ViveAuthApplication {

    @PostConstruct
    public void init() {
        // Set default timezone to Asia/Seoul
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(ViveAuthApplication.class, args);
    }
}