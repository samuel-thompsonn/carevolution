package model.carcontroller;

import model.CollisionEntity;
import model.EvolutionSim;
import model.entity.Car;
import model.entity.EvolutionCar;

public class ManualCarController implements CarController {
  private Car myCar;
  private double myPedalAmount;
  private double myWheelTurn;
  private double myBrakesAmount;

  public ManualCarController(Car car) {
    myCar = car;
  }

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

  @Override
  public void reactToGasPedal(double amount) {
    myPedalAmount += amount;
    myPedalAmount = clamp(myPedalAmount,0.0,1.0);
    myCar.pressPedal(amount);
  }

  private double clamp(double amount, double min, double max) {
    return Math.max(Math.min(amount,max),min);
  }


  @Override
  public void reactToWheelTurn(double turnDegrees) {
    myWheelTurn += turnDegrees;
    myWheelTurn = clamp(myWheelTurn,-EvolutionCar.MAX_WHEEL_ROTATION, EvolutionCar.MAX_WHEEL_ROTATION);
    myCar.turnWheel(turnDegrees);
  }

  @Override
  public void reactToBrakes(double amount) {
    myBrakesAmount += amount;
    myBrakesAmount = clamp(myBrakesAmount,0.0,1.0);
    myCar.pressBrakes(amount);
  }
}
