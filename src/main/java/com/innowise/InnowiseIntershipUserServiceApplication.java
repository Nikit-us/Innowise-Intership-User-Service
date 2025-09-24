package com.innowise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class InnowiseIntershipUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InnowiseIntershipUserServiceApplication.class, args);
    }

}
