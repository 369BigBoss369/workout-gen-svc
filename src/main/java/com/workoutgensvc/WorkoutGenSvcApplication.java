package com.workoutgensvc;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@Slf4j
public class WorkoutGenSvcApplication {

    public static void main(String[] args) {
        log.info("Starting Workout Generation Service application");
        SpringApplication.run(WorkoutGenSvcApplication.class, args);
        log.info("Workout Generation Service application started successfully");
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
