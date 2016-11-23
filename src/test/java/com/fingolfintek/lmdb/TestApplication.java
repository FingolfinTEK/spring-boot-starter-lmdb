package com.fingolfintek.lmdb;


import org.fusesource.lmdbjni.Database;
import org.fusesource.lmdbjni.Env;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestApplication {

    @Bean
    public Database lmdbDatabase(Env lmdbEnvironment) {
        return lmdbEnvironment.openDatabase("test-db");
    }


    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
