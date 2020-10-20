package model.carcontroller;

import model.CollisionEntity;
import model.entity.Car;

public class ManualCarController implements CarController {
  @Override
  public int compareScores(CarController other) {
    return 0;
  }

  @Override
  public CarController produceOffspring(Car car) {
    return null;
  }

  @Override
  public void addToScore() {

  }

  @Override
  public void manipulateCar(double elapsedTime) {

  }

  @Override
  public double getScore() {
    return 0;
  }

  @Override
  public void reactToPointGain(double pointGain) {

  }

  @Override
  public void reactToPositionChange(double newX, double newY) {

  }

  @Override
  public void reactToDirectionChange(double directionDegrees) {

  }

  @Override
  public void reactToRemoval(CollisionEntity entity) {

  }
}
