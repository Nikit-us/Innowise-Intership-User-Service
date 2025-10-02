package com.innowise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableWebSecurity
@EnableMethodSecurity
public class InnowiseIntershipUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InnowiseIntershipUserServiceApplication.class, args);
    }

}
