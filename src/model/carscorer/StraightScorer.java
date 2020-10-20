package model.carscorer;

import model.entity.Car;

public class StraightScorer implements CarScorer {
  @Override
  public double scoreCar(Car car) {
    if (car.getSpeed() < 80 && car.getSpeed() > 70 && car.getWheelTurn() > -10 && car.getWheelTurn() < 10) {
      return 1;
    }
    return 0;
  }
}
