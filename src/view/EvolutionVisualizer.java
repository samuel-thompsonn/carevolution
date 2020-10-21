package view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import model.*;
import model.entity.Car;
import model.entity.WallRegion;

import java.util.ArrayList;
import java.util.List;

public class EvolutionVisualizer extends Parent implements VisualizerListener, SimulationListener {

  public static final double TURN_DEGREES_PER_SECOND = 200;
  public static final int GENERATION_SIZE = 20;
  public static final double PROPORTION_ELIMINATED = 0.50;
  public static final double STEPS_PER_SECOND = 120;
  public static final int SCENE_WIDTH = 1200;
  public static final int SCENE_HEIGHT = 1000;
  private static final double ROUND_DURATION = 10.0;
  private double currentSimTime;
  private Group myRoot;
  private Group myObstacleRoot;
  private List<CollisionEntity> myDeadEntities;
  private CarSim mySimulation;
  private List<EntityVisualizer> myVisualizers;

  private boolean pressingForward;
  private boolean pressingLeft;
  private boolean pressingRight;
  private boolean pressingBack;
  private int myRoundDuration = 10;

  public EvolutionVisualizer(CarSim simulation) {
    mySimulation = simulation;
    currentSimTime = 0;
    myRoot = new Group();
    getChildren().add(myRoot);
    myDeadEntities = new ArrayList<>();
    mySimulation.subscribe(this);
    reactToNewIteration();
    myVisualizers = new ArrayList<>();
    Timeline timeline = new Timeline();
    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1/ STEPS_PER_SECOND), event -> {
      update(1/STEPS_PER_SECOND);
    }));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
    myObstacleRoot = new Group();
    myRoot.getChildren().add(myObstacleRoot);
    this.setOnMouseMoved(event -> {
      reactWithLine(event.getSceneX(),event.getSceneY());});
    this.setOnScroll(event -> rotateLine(event.getDeltaY() / -800));

    this.setOnKeyPressed(event -> {
      if (event.getCode().equals(KeyCode.W)) {
        pressingForward = true;
      }
      if (event.getCode().equals(KeyCode.A)) {
        pressingLeft = true;
      }
      if (event.getCode().equals(KeyCode.D)) {
        pressingRight = true;
      }
      if (event.getCode().equals(KeyCode.S)) {
        pressingBack = true;
      }
      if (event.getCode().equals(KeyCode.SPACE)) {
        clearObstacles();
      }
      if (event.getCode().equals(KeyCode.C)) {
        System.out.println("RESTARTING SIM");
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
        mySimulation.startNewRound();
      }
    });
    this.setOnKeyReleased(event -> {
      if (event.getCode().equals(KeyCode.W)) {
        pressingForward = false;
      }
      if (event.getCode().equals(KeyCode.A)) {
        pressingLeft = false;
      }
      if (event.getCode().equals(KeyCode.D)) {
        pressingRight = false;
      }
      if (event.getCode().equals(KeyCode.S)) {
        pressingBack = false;
      }
      if (event.getCode().equals(KeyCode.J)) {
        setFinderVisibility(false);
      }
    });
    this.setOnMouseClicked(event -> mySimulation.placeObstacle(event.getSceneX(),event.getSceneY(),75,75));
  }

  private void clearObstacles() {
    myObstacleRoot.getChildren().clear();
  }

  private void setFinderVisibility(boolean show) {
    //Should just toggle having distance finder root connected to main root
  }

  private void update(double elapsedSeconds) {
    mySimulation.update(elapsedSeconds);
    if (pressingForward) {
      mySimulation.pressGasPedal(1.0);
    }
    else {
      mySimulation.pressGasPedal(0.0);
    }
    if (pressingLeft) {
      mySimulation.turnWheel(-TURN_DEGREES_PER_SECOND * elapsedSeconds);
    }
    if (pressingRight) {
      mySimulation.turnWheel(TURN_DEGREES_PER_SECOND * elapsedSeconds);
    }
    if (pressingBack) {
      mySimulation.hitBrakes(1.0);
    }
    else {
      mySimulation.hitBrakes(0.0);
    }
  }

  private void restartSimulation() {
    currentSimTime = 0;
    mySimulation.resetCars();
  }

  private void reactWithLine(double x, double y) {
//    mouseLine.setPosition(x,y);
//    mouseLine.calcDistance(0,0,SCENE_WIDTH,SCENE_HEIGHT, myCollisionEntities);
  }

  private void rotateLine(double rotateAmt) {
//    double x = mouseLine.getDirectionX();
//    double y = mouseLine.getDirectionY();
//    mouseLine.setDirection(x*Math.cos(rotateAmt)-y*Math.sin(rotateAmt),x*Math.sin(rotateAmt)+y*Math.cos(rotateAmt));
//    mouseLine.calcDistance(0,0,SCENE_WIDTH,SCENE_HEIGHT, myCollisionEntities);
  }

  private void clearDeadEntities() {
    myDeadEntities.clear();
  }

  @Override
  public void reactToDeath(EntityVisualizer visualizer) {
    myVisualizers.remove(visualizer);
    myRoot.getChildren().remove(visualizer.getGroup());
  }

  @Override
  public void reactToNewIteration() {
    //myRoot.getChildren().clear();
    myVisualizers = new ArrayList<>();
    for (Car car : mySimulation.getCars()) {
      CarVisualizer newCarVis = new CarVisualizer(car);
      myRoot.getChildren().add(newCarVis);
      newCarVis.subscribe(this);
    }
  }

  @Override
  public void reactToNewWall(WallRegion wall) {
    WallVisualizer wallVis = new WallVisualizer(wall);
    myRoot.getChildren().add(wallVis);
    wallVis.subscribe(this);
  }
}
