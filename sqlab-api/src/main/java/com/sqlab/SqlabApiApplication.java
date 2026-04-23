package com.sqlab;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SqlabApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqlabApiApplication.class, args);
        log.info("SQLab API iniciada com sucesso!");
    }

}
