package com.filevault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FileVaultApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FileVaultApplication.class, args);
    }
}
