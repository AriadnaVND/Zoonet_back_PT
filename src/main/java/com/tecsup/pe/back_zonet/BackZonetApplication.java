package com.tecsup.pe.back_zonet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // 💡 IMPORTADO

@SpringBootApplication
@EnableScheduling // 💡 HABILITADO
public class BackZonetApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackZonetApplication.class, args);
    }
}