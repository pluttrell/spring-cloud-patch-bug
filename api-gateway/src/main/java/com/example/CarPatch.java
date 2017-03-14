package com.example;

import com.google.common.base.MoreObjects;


public class CarPatch {

  private String model;

  public CarPatch(String model) {
    this.model = model;
  }

  public String getModel() {
    return model;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CarPatch carPatch = (CarPatch) o;

    return model != null ? model.equals(carPatch.model) : carPatch.model == null;
  }

  @Override
  public int hashCode() {
    return model != null ? model.hashCode() : 0;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("model", model)
        .toString();
  }
}
