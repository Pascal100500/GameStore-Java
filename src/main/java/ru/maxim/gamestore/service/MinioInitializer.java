package ru.maxim.gamestore.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MinioInitializer implements CommandLineRunner {

    private final MinioService minioService;

    public MinioInitializer(MinioService minioService) {
        this.minioService = minioService;
    }

    @Override
    public void run(String... args) {

        try {
            boolean exists = minioService.bucketExists();

            if (exists) {
                System.out.println(
                        "MinIO: bucket gamestore-images exists!"
                );
            } else {
                System.out.println(
                        "MinIO: bucket gamestore-images not found!"
                );
            }

        } catch (Exception e) {
            System.err.println(
                    "MinIO connection error: " + e.getMessage()
            );
        }
    }
}