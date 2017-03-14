package com.example;

import org.springframework.data.annotation.Id;


public class Car {

  @Id
  private String id;
  private String make;
  private String model;

  public Car() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMake() {
    return make;
  }

  public void setMake(String make) {
    this.make = make;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Car car = (Car) o;

    if (id != null ? !id.equals(car.id) : car.id != null) {
      return false;
    }
    if (make != null ? !make.equals(car.make) : car.make != null) {
      return false;
    }
    return model != null ? model.equals(car.model) : car.model == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (make != null ? make.hashCode() : 0);
    result = 31 * result + (model != null ? model.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return com.google.common.base.MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("make", make)
        .add("model", model)
        .toString();
  }

}
