package com.example.boot01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // 스케줄링 기능 활성화
@SpringBootApplication
public class Boot01Application {

    public static void main(String[] args) {
        SpringApplication.run(Boot01Application.class, args);
    }

}
