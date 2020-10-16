import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CarVisualizer extends Parent {
  public static final double MAX_WHEEL_ROTATION = 60.0;
  public static final double FRICTION_FACTOR = 0.01;
  public static final double MAX_ACCELERATION = 10;
  public static final double CAR_RADIUS = 10.0;
  public static final int SENSOR_LENGTH = 800;
  private double myX;
  private double myY;
  private double myRotation;
  private double myWheelRotation;
  private double myPedalPress;
  private double myXDirection;
  private double myYDirection;
  private double mySpeed;
  private ImageView myCarImage;
  private List<DistanceFinder> myDistanceFinders;
  private DistanceFinder myForwardFinder;
  private DistanceFinder myLeftFinder;
  private DistanceFinder myRightFinder;
  private List<CarListener> myListeners;

  public CarVisualizer(double x, double y) {
    myX = x;
    myY = y;
    myRotation = 0;
    try {
      InputStream stream = new FileInputStream("resources/yellowcar.png");
      myCarImage = new ImageView(new Image(stream));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    myCarImage.setX(x);
    myCarImage.setY(y);
    myCarImage.setFitWidth(50);
    myCarImage.setFitHeight(25);
    getChildren().add(myCarImage);
    myDistanceFinders = new ArrayList<>();
    myForwardFinder = (new DistanceFinder(myX,myY, SENSOR_LENGTH,myXDirection,myYDirection));
    myLeftFinder = new DistanceFinder(myX,myY, SENSOR_LENGTH, delayedXDirection(90.0),delayedYDirection(90.0));
    myRightFinder = new DistanceFinder(myX,myY, SENSOR_LENGTH, delayedXDirection(-90.0),delayedYDirection(-90.0));
    myDistanceFinders.add(myForwardFinder);
    myDistanceFinders.add(myRightFinder);
    myDistanceFinders.add(myLeftFinder);
    for (DistanceFinder finder : myDistanceFinders) {
    //  getChildren().add(finder);
    }
    myListeners = new ArrayList<>();
  }

  public void pressPedal(double pressAmount) {
    myPedalPress = Math.max(0.0,Math.min(pressAmount,1.0));
  }

  public void turnWheel(double turnDegrees) {
    myWheelRotation += turnDegrees;
    myWheelRotation = Math.max(-MAX_WHEEL_ROTATION,Math.min(myWheelRotation,MAX_WHEEL_ROTATION));
  }

  /**
   *
   * @param deltaTime The number of seconds since elapsed.
   */
  public void updateSelf(double deltaTime, List<ImpassableRegion> obstacles) {
    double wheelRadians = degreesToRadians(myWheelRotation + myRotation);
    double xComponent = Math.cos(wheelRadians);
    double yComponent = Math.sin(wheelRadians);
    double alignment = (dotProduct2(myXDirection, myYDirection,xComponent,yComponent));

    double accelerationForward = (alignment * (myPedalPress * MAX_ACCELERATION)) - (mySpeed * FRICTION_FACTOR);
    //mySpeed += (myPedalPress * MAX_ACCELERATION) - (mySpeed * FRICTION_FACTOR);
    mySpeed += accelerationForward;

    //Our ACCELERATION, not SPEED, is modified by alignment.
    double xChange = mySpeed * deltaTime * myXDirection;
    double yChange = mySpeed * deltaTime * myYDirection;
    setPosition(myX + xChange, myY + yChange);
    double turnCircumference = (CAR_RADIUS *2*Math.PI);
    double angleChange = (deltaTime * mySpeed) / turnCircumference * myWheelRotation;
    setRotation(myRotation + angleChange);
    findDistances(obstacles);
  }

  public void givePoints(double points) {
    for (CarListener listener : myListeners) {
      listener.reactToPointGain(points);
    }
  }

  void subscribe(CarListener listener) {
    myListeners.add(listener);
  }

  private void findDistances(List<ImpassableRegion> obstacles) {
    for (DistanceFinder finder : myDistanceFinders) {
      finder.calcDistance(0,0,1200,1000,obstacles);
    }
  }

  private double dotProduct2(double x1, double x2, double y1, double y2) {
    return (x1 * y1) + (x2 * y2);
  }

  private double degreesToRadians(double angleDegrees) {
    return (angleDegrees / 360.0) * 2 * Math.PI;
  }

  public void setPosition(double xPos, double yPos) {
    myX = xPos;
    myY = yPos;
    myCarImage.setX(myX);
    myCarImage.setY(myY);
    for (DistanceFinder finder : myDistanceFinders) {
      finder.setPosition(myX,myY);
    }
  }

  public double getPedalPress() {
    return myPedalPress;
  }

  public double getWheelTurn() {
    return myWheelRotation;
  }

  public double getForwardDistance() {
    return myForwardFinder.getDistance();
  }

  public double getLeftDistance() {
    return myLeftFinder.getDistance();
  }

  public double getRightDistance() {
    return myRightFinder.getDistance();
  }

  public double getSpeed() {
    return mySpeed;
  }

  public double getXPos() {
    return myX;
  }

  public double getYPos() {
    return myY;
  }

  public void setRotation(double degreeAmount) {
    myRotation = degreeAmount;
    myXDirection = Math.cos(degreesToRadians(myRotation));
    myYDirection = Math.sin(degreesToRadians(myRotation));
    myCarImage.setRotate(myRotation);
    myForwardFinder.setDirection(myXDirection,myYDirection);
    myRightFinder.setDirection(delayedXDirection(90.0),delayedYDirection(90.0));
    myLeftFinder.setDirection(delayedXDirection(-90.0),delayedYDirection(-90.0));
  }

  private double delayedXDirection(double delayAngleDegrees) {
    return Math.cos(degreesToRadians(myRotation) - degreesToRadians(delayAngleDegrees));
  }

  private double delayedYDirection(double delayAngleDegrees) {
    return Math.sin(degreesToRadians(myRotation) - degreesToRadians(delayAngleDegrees));
  }
}
