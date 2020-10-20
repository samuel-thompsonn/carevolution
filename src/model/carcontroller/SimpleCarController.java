package model.carcontroller;

import model.CollisionEntity;
import model.entity.Car;
import model.entity.EvolutionCar;

import java.util.Arrays;
import java.util.Random;

public class SimpleCarController implements CarController {
  private Car myCar;
  private double[][] myFunction;
  private double myScore;
  private Random myRandom;

  public SimpleCarController(Car car) {
    myCar = car;
    myFunction = new double[2][6];
    for (double[] column : myFunction) {
      for (int i = 0; i < column.length; i ++) {
        column[i] = (new Random().nextDouble() * 2) - 1.0;
      }
    }
  }

  public SimpleCarController(Car car, double[][] parentFunction) {
    myCar = car;
    myCar.subscribe(this);
    myFunction = parentFunction.clone();
    for (double[] column : myFunction) {
      for (int i = 0; i < column.length; i ++) {
        //change by +-10 percent
        double percentVariation = 0.50;
        double variationRange = (percentVariation * column[i]) * 2;
        column[i] += (new Random().nextDouble() * variationRange) - (variationRange / 2.0);
      }
      //System.out.println(Arrays.toString(column));
    }
  }

  public void manipulateCar(double elapsedTime) {
    double[] actions = calculateMovements();
    myCar.pressPedal(actions[0]);
    myCar.turnWheel(actions[1]);
//    System.out.println("actions[1] = " + actions[1]);
//    myCar.turnWheel(new Random().nextInt(200) - 100);
  }

  private double[] calculateMovements() {
    double[] carParams = {
            myCar.getPedalPress(),
            myCar.getWheelTurn() / 120.0,
            myCar.getForwardDistance() / EvolutionCar.SENSOR_LENGTH,
            myCar.getLeftDistance() / EvolutionCar.SENSOR_LENGTH,
            myCar.getRightDistance() / EvolutionCar.SENSOR_LENGTH,
            myCar.getSpeed() / 200.0
    };
    System.out.println("carParams.toString() = " + Arrays.toString(carParams));
    double[] output = new double[2];
    for (int i = 0; i < 2; i ++) {
      output[i] = dotProduct(myFunction[i],carParams);
    }
    return output;
  }

  private double dotProduct(double[] x, double[] y) {
    if (x.length != y.length) {
      System.out.println("Trying to dot product uneven lengths");
      return 0;
    }
    double total = 0;
    for (int i = 0; i < x.length; i ++) {
      total += x[i] * y[i];
    }
    return total;
  }

  public void addToScore() {
    //Let's prioritize going straight and fast as much as possible.
    double angleMultiplier = Math.abs((Math.abs(myCar.getWheelTurn()) - 60)) / 60.0;
    myScore += angleMultiplier * myCar.getSpeed();
    if (myScore < 0) {
      System.out.println("myCar.getSpeed() = " + myCar.getSpeed());
      System.out.println("angleMultiplier = " + angleMultiplier);
    }
    //TODO: Make this a reaction to the car, using listener pattern
    //Reset score if we get our position reset.
    if (myCar.getXPos() == 400 && myCar.getYPos() == 300) {
      //myScore = 0;
    }
  }

  public void setScore(double score) {
    myScore = score;
  }

  public double getScore() {
    return myScore;
  }

  public SimpleCarController produceOffspring(Car car) {
    return new SimpleCarController(car, myFunction);
  }

  public int compareScores(CarController other) {
    //return Double.compare(getScore(), other.getScore());
    return 1;
  }

  @Override
  public void reactToPointGain(double pointGain) {
    myScore += pointGain;
  }

  @Override
  public void reactToPositionChange(double newX, double newY) {

  }

  @Override
  public void reactToDirectionChange(double directionDegrees) {

  }

  @Override
  public void reactToRemoval(CollisionEntity entity) {
    //Doesn't care if the car is dead.
  }
}
