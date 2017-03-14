package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.map.repository.config.EnableMapRepositories;


@SpringBootApplication
@EnableMapRepositories
public class CarServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CarServiceApplication.class, args);
  }

}
