package com.example.secondhand_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.secondhand_backend.mapper")
public class SecondhandBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondhandBackendApplication.class, args);
    }

}
