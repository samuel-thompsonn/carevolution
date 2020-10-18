import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class MainVisualizer extends Application {

  public static final double TURN_DEGREES_PER_SECOND = 500;
  public static final int GENERATION_SIZE = 20;
  public static final double PROPORTION_ELIMINATED = 0.90;
  public static final double STEPS_PER_SECOND = 120;
  public static final int SCENE_WIDTH = 1200;
  public static final int SCENE_HEIGHT = 1000;
  private DistanceFinder mouseLine;
  private CarVisualizer myCar;
  private Group myCarRoot;
  private List<CarVisualizer> myCars;
  private List<CarController> myControllers;
  private double currentSimTime;
  private Group myRoot;
  private CarScorer myScorer;
  private List<ImpassableRegion> myObstacles;
  private Group myObstacleRoot;

  private boolean pressingForward;
  private boolean pressingLeft;
  private boolean pressingRight;
  private int myRoundDuration = 10;

  @Override
  public void start(Stage primaryStage) {
    currentSimTime = 0;
    mouseLine = new DistanceFinder(400,300,500,1,0);
    myRoot = new Group();
    myCars = new ArrayList<>();
    myRoot.getChildren().add(mouseLine);
    myCar = new CarVisualizer(400,200);
    myCarRoot = new Group();
    myRoot.getChildren().add(myCar);
    myRoot.getChildren().add(myCarRoot);
    myCars.add(myCar);
    myControllers = initializeControllers();
    Scene mainScene = new Scene(myRoot, SCENE_WIDTH, SCENE_HEIGHT);
    mainScene.setOnMouseMoved(event -> {
      reactWithLine(event.getSceneX(),event.getSceneY());});
    mainScene.setOnScroll(event -> rotateLine(event.getDeltaY() / -800));
    myScorer = new DistanceScorer();

    mainScene.setOnKeyPressed(event -> {
      if (event.getCode().equals(KeyCode.W)) {
        pressingForward = true;
      }
      if (event.getCode().equals(KeyCode.A)) {
        pressingLeft = true;
      }
      if (event.getCode().equals(KeyCode.D)) {
        pressingRight = true;
      }
      if (event.getCode().equals(KeyCode.SPACE)) {
        clearObstacles();
      }
      if (event.getCode().equals(KeyCode.C)) {
        restartSimulation();
      }
      if (event.getCode().equals(KeyCode.J)) {
        setFinderVisibility(true);
      }
      if (event.getCode().equals(KeyCode.O)) {
        myRoundDuration += 5;
      }
      if (event.getCode().equals(KeyCode.P)) {
        if (myRoundDuration > 7.5) {
          myRoundDuration -= 5;
        }
      }
      if (event.getCode().equals(KeyCode.ENTER)) {
        startNewRound();
      }
    });
    mainScene.setOnKeyReleased(event -> {
      if (event.getCode().equals(KeyCode.W)) {
        pressingForward = false;
      }
      if (event.getCode().equals(KeyCode.A)) {
        pressingLeft = false;
      }
      if (event.getCode().equals(KeyCode.D)) {
        pressingRight = false;
      }
      if (event.getCode().equals(KeyCode.J)) {
        setFinderVisibility(false);
      }
    });
    mainScene.setOnMouseClicked(event -> placeObstacle(event.getSceneX(),event.getSceneY(),75,75));

    Timeline timeline = new Timeline();
    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1/ STEPS_PER_SECOND), event -> {
      update(1/60.0);
    }));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
    primaryStage.setScene(mainScene);
    primaryStage.setTitle("Distance Finder Test");
    primaryStage.show();
    myObstacles = new ArrayList<>();
    myObstacleRoot = new Group();
    myRoot.getChildren().add(myObstacleRoot);
  }

  //TODO: Address the fact that this is a state modifying function with a return value.
  private List<CarController> initializeControllers() {
    List<CarController> controllers = new ArrayList<>();
    for (int i = 0; i < GENERATION_SIZE; i ++) {
      CarVisualizer autoCar = new CarVisualizer(400,300);
      myCarRoot.getChildren().add(autoCar);
      myCars.add(autoCar);
      CarController autoController = new NeuralCarController(autoCar);
      controllers.add(autoController);
    }
    return controllers;
  }

  private void placeObstacle(double xPos, double yPos, double width, double height) {
    WallRegion region = new WallRegion(xPos, yPos, width, height);
    myObstacleRoot.getChildren().add(region);
    myObstacles.add(region);
  }

  private void clearObstacles() {
    myObstacles = new ArrayList<>();
    myObstacleRoot.getChildren().clear();
  }

  private void setFinderVisibility(boolean show) {
    for (CarVisualizer car : myCars) {
      car.showFinders(show);
    }
  }


  private void update(double elapsedSeconds) {
    myCar.pressPedal(0.0);
    if (pressingForward) {
      myCar.pressPedal(1.0);
    }
    if (pressingLeft) {
      myCar.turnWheel(-TURN_DEGREES_PER_SECOND * elapsedSeconds);
    }
    if (pressingRight) {
      myCar.turnWheel(TURN_DEGREES_PER_SECOND * elapsedSeconds);
    }
    processAutoCars(elapsedSeconds);
    List<CarVisualizer> deadCars = new ArrayList<>();
    for (CarVisualizer car : myCars) {
      car.updateSelf(elapsedSeconds, myObstacles);
      car.givePoints(myScorer.scoreCar(car));
      if (car.getXPos() < 0 || car.getXPos() > SCENE_WIDTH || car.getYPos() < 0 || car.getYPos() > SCENE_HEIGHT) {
        penalizeCar(car);
        deadCars.add(car);
        continue;
      }
      for (ImpassableRegion region : myObstacles) {
        if (region.pointInBounds(car.getXPos(),car.getYPos())) {
          penalizeCar(car);
          deadCars.add(car);
        }
      }
    }
    for (CarVisualizer deadCar : deadCars) {
      myCars.remove(deadCar);
    }
    currentSimTime += elapsedSeconds;
    if (currentSimTime >= myRoundDuration) {
      startNewRound();
    }
  }

  private void penalizeCar(CarVisualizer car) {
    car.setPosition(-1000,300);
    //car.givePoints(-1000000);
  }

  private void startNewRound() {
    currentSimTime = 0;
    myCars.clear();
    myCarRoot.getChildren().clear();
    myCars.add(myCar);
    myControllers.sort(CarController::compareScores);
    List<CarController> nextGeneration = new ArrayList<>();
    if (myControllers.size() < 1) { return; }
    System.out.println("Highest scores:\n"  + "1: " + myControllers.get(myControllers.size()-1).getScore() + "\n"
                                            + "2: " + myControllers.get(myControllers.size()-2).getScore() + "\n"
                                            + "3: " + myControllers.get(myControllers.size()-3).getScore());
    for (int j = (int)(PROPORTION_ELIMINATED * myControllers.size()); j < myControllers.size(); j ++) {
      CarController remaining = myControllers.get(j);
      for (int i = 0; i < (int)(1 / (1-PROPORTION_ELIMINATED)); i ++) {
        CarVisualizer newCar = new CarVisualizer(400,300);
        nextGeneration.add(remaining.produceOffspring(newCar));
        myCars.add(newCar);
        myCarRoot.getChildren().add(newCar);
      }
    }
    System.out.println("----------------------------------------");
    myControllers = nextGeneration;
    System.out.println("myControllers.size() = " + myControllers.size());
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
    myCarRoot.getChildren().clear();
    myCars.add(myCar);
    myControllers = initializeControllers();
  }

  private void reactWithLine(double x, double y) {
    mouseLine.setPosition(x,y);
    mouseLine.calcDistance(0,0,SCENE_WIDTH,SCENE_HEIGHT, myObstacles);
  }

  private void rotateLine(double rotateAmt) {
    double x = mouseLine.getDirectionX();
    double y = mouseLine.getDirectionY();
    mouseLine.setDirection(x*Math.cos(rotateAmt)-y*Math.sin(rotateAmt),x*Math.sin(rotateAmt)+y*Math.cos(rotateAmt));
    mouseLine.calcDistance(0,0,SCENE_WIDTH,SCENE_HEIGHT, myObstacles);
  }

  private void turnCarWheel(double degrees) {
    myCar.turnWheel(degrees);
  }

  private void pressCarPedal(double amount) {
    myCar.pressPedal(amount);
  }
}
