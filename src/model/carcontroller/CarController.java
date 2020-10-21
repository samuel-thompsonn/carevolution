package model.carcontroller;

import model.entity.Car;
import model.entity.CarListener;

public interface CarController extends CarListener {
  public int compareScores(CarController other);
  public CarController produceOffspring(Car car); //TODO: Interfaces shall not rely on implementations.
  public void addToScore();
  public void manipulateCar(double elapsedTime);
  public double getScore();
  default void reactToGasPedal(double amount) {
    //do nothing
  }
  default void reactToWheelTurn(double turnDegrees) {
    //do nothing
  }
  default void reactToBrakes(double amount) {
    //do nothing
  }
}
