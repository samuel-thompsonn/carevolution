package model;

import model.carcontroller.CarController;
import model.carcontroller.ManualCarController;
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

  public static final int SCENE_WIDTH = 1200;
  public static final int SCENE_HEIGHT = 1000;
  public static final int CAR_START_X_RANGE = 1;
  public static final int CAR_START_Y_RANGE = 1;
  public static final int CAR_X_OFFSET = 150;
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
        if (entitiesColliding(entity,otherEntity)) {
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

  private boolean entitiesColliding(CollisionEntity a, CollisionEntity b) {
    //Wide phase comes first
    if (!entitiesWideColliding(a,b)) {
      return false;
    }
    //For each line l of a,b
    List<double[]> lines = new ArrayList<>();
    lines.addAll(getPolygonLines(a));
    lines.addAll(getPolygonLines(b));
    //Find the orthogonal line q
    List<double[]> orthogonalLines = new ArrayList<>();
    for (double[] line : lines) {
      orthogonalLines.add(orthogonal2d(line));
    }
    for (double[] orthogonal : orthogonalLines) {
      if (isSeparatingAxis(orthogonal,a,b)) {
        //System.out.println("orthogonal = " + Arrays.toString(orthogonal));
        return false;
      }
    }
    return true;
  }

  private boolean entitiesWideColliding(CollisionEntity a, CollisionEntity b) {
    List<double[]> aPoints = a.getHitboxRectPoints();
    List<double[]> bPoints = b.getHitboxRectPoints();
    double[] aMin = aPoints.get(0);
    double[] aMax = aPoints.get(1);
    double[] bMin = bPoints.get(0);
    double[] bMax = bPoints.get(1);
    boolean xColliding = !(aMax[0] < bMin[0] || bMax[0] < aMin[0]);
    boolean yColliding = !(aMax[1] < bMin[1] || bMax[1] < aMin[1]);
    return (xColliding && yColliding);
  }

  private List<double[]> getRectBoundPoints(CollisionEntity entity) {
    List<double[]> points = entity.getHitboxPoints();
    List<double[]> boundPoints = new ArrayList<>();
    double minX = 0, minY = 0, maxX = 0, maxY = 0;
    for (double[] point : points) {
      minX = Math.min(point[0],minX);
      minY = Math.min(point[1],minY);
      maxX = Math.max(point[0],maxX);
      maxY = Math.max(point[1],maxY);
    }
    boundPoints.add(new double[]{minX,minY});
    boundPoints.add(new double[]{maxX,maxY});
    return boundPoints;
  }

  private boolean isSeparatingAxis(double[] line, CollisionEntity a, CollisionEntity b) {
    //Returns true if the projections of the two collision entities onto the line
    // do NOT overlap at all.
    double aMin = minVertexProjection(a.getHitboxPoints(),line);
    double aMax = maxVertexProjection(a.getHitboxPoints(),line);
    double bMin = minVertexProjection(b.getHitboxPoints(),line);
    double bMax = maxVertexProjection(b.getHitboxPoints(),line);
    return (aMax < bMin || bMax < aMin);
  }

  private double minVertexProjection(List<double[]> points, double[] projLine) {
    double aMin = Double.MAX_VALUE;
    for (double[] vertex : points) {
      double projectionDot = LinearUtil.dotProduct(projLine,vertex);
      aMin = Math.min(aMin,projectionDot);
    }
    return aMin;
  }

  private double maxVertexProjection(List<double[]> points, double[] projLine) {
    double aMax = Double.MIN_VALUE;
    for (double[] vertex : points) {
      double projectionDot = LinearUtil.dotProduct(projLine,vertex);
      aMax = Math.max(aMax,projectionDot);
    }
    return aMax;
  }

  private List<double[]> getPolygonLines(CollisionEntity entity) {
    List<double[]> aLines = new ArrayList<>();
    List<double[]> aPoints = entity.getHitboxPoints();
    for (int i = 0; i < aPoints.size(); i ++) {
      int next = i+1;
      if (next == aPoints.size()) {
        next = 0;
      }
      aLines.add(new double[]{
              aPoints.get(i)[0] - aPoints.get(next)[0],
              aPoints.get(i)[1] - aPoints.get(next)[1]
      });
    }
    return aLines;
  }

  private double[] orthogonal2d(double[] vector) {
    return new double[] {-vector[1],vector[0]};
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
    List<CarController> survivors = new ArrayList<>();
    for (int j = (int)(myProportionEliminated * myControllers.size()); j < myControllers.size(); j ++) {
      survivors.add(myControllers.get(j));
//      CarController remaining = myControllers.get(j);
//      for (int i = 0; i < (int)(1 / (1-myProportionEliminated)); i ++) {
//        Car newCar = new EvolutionCar(new Random().nextDouble() * CAR_START_X_RANGE + CAR_X_OFFSET,new Random().nextDouble() * CAR_START_Y_RANGE + CAR_Y_OFFSET);
//        newCar.subscribe(this);
//        nextGeneration.add(remaining.produceOffspring(newCar));
//        myCars.add(newCar);
//        myCollisionEntities.add(newCar);
//      }
    }
    while (nextGeneration.size() < myGenerationSize) {
      CarController remaining = survivors.get(new Random().nextInt(survivors.size()));
      Car newCar = new EvolutionCar(new Random().nextDouble() * CAR_START_X_RANGE + CAR_X_OFFSET,new Random().nextDouble() * CAR_START_Y_RANGE + CAR_Y_OFFSET);
      newCar.subscribe(this);
      nextGeneration.add(remaining.produceOffspring(newCar));
      myCars.add(newCar);
      myCollisionEntities.add(newCar);
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

  @Override
  public void pressGasPedal(double amount) {
    for (CarController controller : myControllers) {
      controller.reactToGasPedal(amount);
    }
  }

  @Override
  public void turnWheel(double turnDegrees) {
    for (CarController controller : myControllers) {
      controller.reactToWheelTurn(turnDegrees);
    }
  }

  @Override
  public void hitBrakes(double amount) {
    for (CarController controller : myControllers) {
      controller.reactToBrakes(amount);
    }
  }
}
