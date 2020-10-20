package view;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import model.CollisionEntity;
import model.entity.Car;
import model.entity.CarListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CarVisualizer extends Parent implements CarListener, EntityVisualizer {
  public static final double MAX_WHEEL_ROTATION = 40;
  public static final double FRICTION_FACTOR = 0.01;
  public static final double MAX_ACCELERATION = 10;
  public static final double CAR_RADIUS = 10.0;
  public static final int SENSOR_LENGTH = 300;
  public static final int CAR_LENGTH = 50;
  public static final int CAR_WIDTH = 25;
  private double myX;
  private double myY;
  private double myRotation;
  private double myWheelRotation;
  private ImageView myCarImage;
  private Rectangle myHitbox;
  private List<VisualizerListener> myListeners;
  private Car myCar;
  private Line myLeftFinder;
  private Line myLeftForwardFinder;
  private Line myForwardFinder;
  private Line myRightForwardFinder;
  private Line myRightFinder;
  private List<Line> myFinderVisuals;

  private boolean showingFinders;

  public CarVisualizer(Car car) {
    myCar = car;
    myRotation = 0;
    try {
      InputStream stream = new FileInputStream("resources/yellowcar.png");
      myCarImage = new ImageView(new Image(stream));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    initFinders();
    myCarImage.setFitWidth(CAR_LENGTH);
    myCarImage.setFitHeight(CAR_WIDTH);
    getChildren().add(myCarImage);
    myHitbox = new Rectangle(car.getCollisionX(),car.getCollisionY(), car.getCollisionWidth(),car.getCollisionHeight());
    myHitbox.setFill(Color.color(1.0,0.0,0.0,0.3));
    getChildren().add(myHitbox);
    myListeners = new ArrayList<>();

    showingFinders = false;
    car.subscribe(this);
    setPosition(car.getXPos(),car.getYPos());
    refreshFinders();
  }

  private void initFinders() {
    myLeftFinder = new Line();
    myLeftForwardFinder = new Line();
    myForwardFinder = new Line();
    myRightForwardFinder = new Line();
    myRightFinder = new Line();
    myFinderVisuals = new ArrayList<>();
    myFinderVisuals.add(myLeftFinder);
    myFinderVisuals.add(myLeftForwardFinder);
    myFinderVisuals.add(myForwardFinder);
    myFinderVisuals.add(myRightForwardFinder);
    myFinderVisuals.add(myRightFinder);
    getChildren().addAll(myFinderVisuals);
  }

  public void setPosition(double xPos, double yPos) {
    myX = xPos;
    myY = yPos;
    double middleX = myX - (CAR_LENGTH * 0.5);
    double middleY = myY - (CAR_WIDTH * 0.5);
    myCarImage.setX(middleX);
    myCarImage.setY(middleY);
    myHitbox.setX(xPos);
    myHitbox.setY(yPos);
    refreshFinders();
  }

  public double getXPos() {
    return myX;
  }

  public double getYPos() {
    return myY;
  }

  public void setRotation(double degreeAmount) {
    myRotation = degreeAmount;
    myCarImage.setRotate(myRotation);
    refreshFinders();
  }

  private void refreshFinders() {
    for (Line finder : myFinderVisuals) {
      finder.setStartX(myX);
      finder.setStartY(myY);
    }
    myLeftFinder.setEndX(finderEndX(90,myCar.getLeftDistance()));
    myLeftFinder.setEndY(finderEndY(90,myCar.getLeftDistance()));
    myLeftForwardFinder.setEndX(finderEndX(45,myCar.getForwardLeftDistance()));
    myLeftForwardFinder.setEndY(finderEndY(45,myCar.getForwardLeftDistance()));
    myForwardFinder.setEndX(finderEndX(0,myCar.getForwardDistance()));
    myForwardFinder.setEndY(finderEndY(0,myCar.getForwardDistance()));
    myRightForwardFinder.setEndX(finderEndX(-45,myCar.getForwardRightDistance()));
    myRightForwardFinder.setEndY(finderEndY(-45,myCar.getForwardRightDistance()));
    myRightFinder.setEndX(finderEndX(-90,myCar.getRightDistance()));
    myRightFinder.setEndY(finderEndY(-90,myCar.getRightDistance()));
  }

  private double finderEndX(double angleDiffDegrees, double length) {
    return myX + (Math.cos(degreesToRadians(myRotation + angleDiffDegrees)) * length);
  }

  private double finderEndY(double angleDiffDegrees, double length) {
    return myY + (Math.sin(degreesToRadians(myRotation + angleDiffDegrees)) * length);
  }

  private double degreesToRadians(double degrees) {
    return (degrees / 360.0) * 2 * Math.PI;
  }

  @Override
  public void reactToPointGain(double pointGain) {
    // Doesn't care (yet)
  }

  @Override
  public void reactToPositionChange(double newX, double newY) {
    setPosition(newX,newY);
  }

  @Override
  public void reactToDirectionChange(double directionDegrees) {
    setRotation(directionDegrees);
  }

  @Override
  public void reactToRemoval(CollisionEntity entity) {
    //TODO: just pass the message up through the ranks I guess?
    for (VisualizerListener listener : myListeners) {
      listener.reactToDeath(this);
    }
  }

  @Override
  public Parent getGroup() {
    return this;
  }

  @Override
  public void subscribe(VisualizerListener listener) {
    myListeners.add(listener);
  }
}
