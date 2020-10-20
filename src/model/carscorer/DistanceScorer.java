package model.carscorer;

import model.entity.Car;

public class DistanceScorer implements CarScorer {
  @Override
  public double scoreCar(Car car) {
    return car.getSpeed();
  }
}
