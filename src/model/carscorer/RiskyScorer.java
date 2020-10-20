package model.carscorer;

import model.entity.Car;

public class RiskyScorer implements CarScorer {
  public double scoreCar(Car car) {
    //Reset score if we get our position reset.
    if (car.getXPos() == 400 && car.getYPos() == 300) {
      return 0;
    }
    //double angleMultiplier = Math.abs((Math.abs(car.getWheelTurn()) - 60)) / 60.0;
    //rewards going straight, penalizes going too turny
    double angleMultiplier = (-1.0 * Math.abs(car.getWheelTurn()) + 30) / 60;
    //penalizes going slowly and prioritizes going straight
    double angleMult2 = Math.max(((-1.0 * Math.abs(car.getWheelTurn())) + 30) / 30,0);
    double speedScore = (car.getSpeed() - 100) / 100;
    //return angleMultiplier * car.getSpeed();
    return angleMult2 * speedScore;
  }
}
