package com.jobboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.jobboard.config.EnvLoader;

@SpringBootApplication
public class JobBoardApplication {
    public static void main(String[] args) {

        EnvLoader.load();
        SpringApplication.run(JobBoardApplication.class, args);
    }
}