package com.example.secondhand_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecondhandBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondhandBackendApplication.class, args);
        System.out.println("启动成功");
        System.out.println("http://localhost:8080/api/swagger-ui/index.html");
    }

}
