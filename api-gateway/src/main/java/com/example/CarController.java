package com.example;


import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;


@RestController
public class CarController {

  private RestTemplate restTemplate;

  public CarController(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @PostMapping("/cars")
  public String addCar(@RequestBody Car car) {

    System.out.println("POSTing to /cars: " + car);

    URI resourceUri = restTemplate.postForLocation(URI.create("http://localhost:8080/cars"), car);

    System.out.println("New Car added. URI: " + resourceUri);

    return resourceUri.toString();

  }

  @PatchMapping("/cars/{carId}/{model}")
  public void updateCar(@PathVariable("carId") String carId, @PathVariable("model") String model) {

    CarPatch carPatch = new CarPatch(model);

    System.out.println("PATCHing to /cars/" + carId + " with " + carPatch);

    Car updatedCar = restTemplate.patchForObject(URI.create("http://localhost:8080/cars/" + carId), carPatch, Car.class);

    System.out.println("Results: " + updatedCar);

  }

}
