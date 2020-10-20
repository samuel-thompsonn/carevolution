package model;

import model.carcontroller.CarController;
import model.carcontroller.NeuralCarController;
import model.carscorer.CarScorer;
import model.carscorer.DistanceScorer;
import model.entity.Car;
import model.entity.EvolutionCar;
import model.entity.WallRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EvolutionSim implements CarSim, CollisionEntityListener {

  public static final double TURN_DEGREES_PER_SECOND = 500;
  public static final double STEPS_PER_SECOND = 120;
  public static final int SCENE_WIDTH = 1200;
  public static final int SCENE_HEIGHT = 1000;
  public static final int CAR_START_X_RANGE = 1;
  public static final int CAR_START_Y_RANGE = 1;
  public static final int CAR_X_OFFSET = 50;
  public static final int CAR_Y_OFFSET = 150;
  private List<Car> myCars;
  private List<CarController> myControllers;
  private List<WallRegion> myWalls;
  private double currentSimTime;
  private CarScorer myScorer;
  private List<CollisionEntity> myCollisionEntities;
  private List<CollisionEntity> myDeadEntities;
  private List<SimulationListener> myListeners;

  private boolean pressingForward;
  private boolean pressingLeft;
  private boolean pressingRight;
  private double myRoundDuration;
  private int myGenerationSize;
  private double myProportionEliminated;

  public EvolutionSim(int generationSize, double proportionEliminated, double roundDuration) {
    myGenerationSize = generationSize;
    myProportionEliminated = proportionEliminated;
    myRoundDuration = roundDuration;
    currentSimTime = 0;
    myCars = new ArrayList<>();
    myWalls = new ArrayList<>();
    myCollisionEntities = new ArrayList<>();
    myDeadEntities = new ArrayList<>();
    myListeners = new ArrayList<>();
    initializeControllers();
    myScorer = new DistanceScorer();
  }

  public List<Car> getCars() {
    return new ArrayList<>(myCars);
  }

  public List<WallRegion> getWalls() {
    return new ArrayList<>(myWalls);
  }

  //TODO: Address the fact that this is a state modifying function with a return value.
  private void initializeControllers() {
    currentSimTime = 0;
    myCollisionEntities.removeAll(myCars);
    myCars.clear();
    myControllers = new ArrayList<>();
    for (int i = 0; i < myGenerationSize; i ++) {
      Car autoCar = new EvolutionCar(new Random().nextDouble() * CAR_START_X_RANGE + CAR_X_OFFSET,new Random().nextDouble() * CAR_START_Y_RANGE + CAR_Y_OFFSET);
      myCars.add(autoCar);
      myCollisionEntities.add(autoCar);
      autoCar.subscribe(this);
      CarController autoController = new NeuralCarController(autoCar);
      myControllers.add(autoController);
    }
    System.out.println(myListeners.size());
    for (SimulationListener listener : myListeners) {
      listener.reactToNewIteration();
    }
  }

  public void placeObstacle(double xPos, double yPos, double width, double height) {
    WallRegion region = new WallRegion(xPos, yPos, width, height);
    myCollisionEntities.add(region);
    myWalls.add(region);
    for (SimulationListener listener : myListeners) {
      listener.reactToNewWall(region);
    }
  }

  public void clearObstacles() {
    myCollisionEntities.removeAll(myWalls);
    myWalls.clear();
  }

  public void update(double elapsedSeconds) {
    processAutoCars(elapsedSeconds);
    updateCars(elapsedSeconds);
    for (CollisionEntity entity : myCollisionEntities) {
      //Remember: Don't let an entity collide with itself!
      for (CollisionEntity otherEntity : myCollisionEntities) {
        if (otherEntity == entity) {
          continue;
        }
        boolean xColliding = !(entity.getCollisionX() + entity.getCollisionWidth() < otherEntity.getCollisionX()) &&
                !(otherEntity.getCollisionX() + otherEntity.getCollisionWidth() < entity.getCollisionX());
        boolean yColliding = !(entity.getCollisionY() + entity.getCollisionHeight() < otherEntity.getCollisionY()) &&
                !(otherEntity.getCollisionY() + otherEntity.getCollisionHeight() < entity.getCollisionY());
        if (xColliding && yColliding) {
          entity.handleEntityCollision(otherEntity);
        }
      }
    }
    clearDeadEntities();
    currentSimTime += elapsedSeconds;
    if (currentSimTime >= myRoundDuration) {
      startNewRound();
    }
  }

  private void updateCars(double elapsedSeconds) {
    for (Car car : myCars) {
      if (!myCollisionEntities.contains(car)) {
        continue;
      }
      car.updateSelf(elapsedSeconds, myCollisionEntities);
      car.givePoints(myScorer.scoreCar(car));
      if (car.getXPos() < 0 || car.getXPos() > SCENE_WIDTH || car.getYPos() < 0 || car.getYPos() > SCENE_HEIGHT) {
        penalizeCar(car);
        myDeadEntities.add(car);
      }
    }
  }

  private void penalizeCar(Car car) {
    car.setPosition(-1000,300);
  }

  public void startNewRound() {
    currentSimTime = 0;
    myCollisionEntities.removeAll(myCars);
    for (Car car : myCars) {
      car.destroy();
    }
    myCars.clear();
    myControllers.sort(CarController::compareScores);
    List<CarController> nextGeneration = new ArrayList<>();
    if (myControllers.size() < 1) { return; }
    System.out.println("Highest scores:\n"  + "1: " + myControllers.get(myControllers.size()-1).getScore() + "\n"
            + "2: " + myControllers.get(myControllers.size()-2).getScore() + "\n"
            + "3: " + myControllers.get(myControllers.size()-3).getScore());
    for (int j = (int)(myProportionEliminated * myControllers.size()); j < myControllers.size(); j ++) {
      CarController remaining = myControllers.get(j);
      for (int i = 0; i < (int)(1 / (1-myProportionEliminated)); i ++) {
        Car newCar = new EvolutionCar(new Random().nextDouble() * CAR_START_X_RANGE + CAR_X_OFFSET,new Random().nextDouble() * CAR_START_Y_RANGE + CAR_Y_OFFSET);
        newCar.subscribe(this);
        nextGeneration.add(remaining.produceOffspring(newCar));
        myCars.add(newCar);
        myCollisionEntities.add(newCar);
      }
    }
    System.out.println("----------------------------------------");
    myControllers = nextGeneration;
    System.out.println("myControllers.size() = " + myControllers.size());
    System.out.println(myListeners.size());
    for (SimulationListener listener : myListeners) {
      System.out.println("Notifying listener.");
      listener.reactToNewIteration();
    }
  }

  private void processAutoCars(double elapsedSeconds) {
    for (CarController auto : myControllers) {
      auto.addToScore();
      auto.manipulateCar(elapsedSeconds);
    }
  }

  private void restartSimulation() {
    currentSimTime = 0;
    myCars.clear();
    initializeControllers();
  }

  private void reactWithLine(double x, double y) {
  }

  private void rotateLine(double rotateAmt) {
  }

  @Override
  public void reactToRemoval(CollisionEntity removed) {
    myDeadEntities.add(removed);
  }

  private void clearDeadEntities() {
    myCollisionEntities.removeAll(myDeadEntities);
    myDeadEntities.clear();
  }

  public void subscribe(SimulationListener listener) {
    myListeners.add(listener);
  }

  @Override
  public void resetCars() {
    for (Car car : myCars) {
      car.destroy();
    }
    initializeControllers();
  }
}
