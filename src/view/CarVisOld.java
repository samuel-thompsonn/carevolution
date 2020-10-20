//package view;
//
//import javafx.scene.Group;
//import javafx.scene.Parent;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import model.CarListener;
//import model.CollisionEntity;
//import model.CollisionEntityListener;
//import model.DistanceFinder;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//public class CarVisOld {
//}
//package view;
//
//        import javafx.scene.Group;
//        import javafx.scene.Parent;
//        import javafx.scene.image.Image;
//        import javafx.scene.image.ImageView;
//        import model.CarListener;
//        import model.CollisionEntity;
//        import model.CollisionEntityListener;
//        import model.DistanceFinder;
//
//        import java.io.FileInputStream;
//        import java.io.FileNotFoundException;
//        import java.io.InputStream;
//        import java.util.ArrayList;
//        import java.util.List;
//
//public class CarVisualizer extends Parent implements CollisionEntity {
//  public static final double MAX_WHEEL_ROTATION = 40;
//  public static final double FRICTION_FACTOR = 0.01;
//  public static final double MAX_ACCELERATION = 10;
//  public static final double CAR_RADIUS = 10.0;
//  public static final int SENSOR_LENGTH = 300;
//  public static final int CAR_LENGTH = 50;
//  public static final int CAR_WIDTH = 25;
//  private double myX;
//  private double myY;
//  private double myRotation;
//  private double myWheelRotation;
//  private double myPedalPress;
//  private double myXDirection;
//  private double myYDirection;
//  private double mySpeed;
//  private ImageView myCarImage;
//  private List<DistanceFinder> myDistanceFinders;
//  private DistanceFinder myForwardFinder;
//  private DistanceFinder myLeftFinder;
//  private DistanceFinder myForwardLeftFinder;
//  private DistanceFinder myRightFinder;
//  private DistanceFinder myForwardRightFinder;
//  private List<CarListener> myListeners;
//  private List<CollisionEntityListener> myCollisionListeners;
//  private boolean showingFinders;
//  private Group myDistanceFinderRoot;
//
//  public CarVisualizer(double x, double y) {
//    myRotation = 0;
//    try {
//      InputStream stream = new FileInputStream("resources/yellowcar.png");
//      myCarImage = new ImageView(new Image(stream));
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    }
//    myCarImage.setFitWidth(CAR_LENGTH);
//    myCarImage.setFitHeight(CAR_WIDTH);
//    getChildren().add(myCarImage);
//    myDistanceFinders = new ArrayList<>();
//    myForwardFinder = (new DistanceFinder(myX,myY, SENSOR_LENGTH,myXDirection,myYDirection));
//    myLeftFinder = new DistanceFinder(myX,myY, SENSOR_LENGTH, delayedXDirection(90.0),delayedYDirection(90.0));
//    myForwardLeftFinder = new DistanceFinder(myX,myY, SENSOR_LENGTH, delayedXDirection(45.0),delayedYDirection(45.0));
//    myRightFinder = new DistanceFinder(myX,myY, SENSOR_LENGTH, delayedXDirection(-90.0),delayedYDirection(-90.0));
//    myForwardRightFinder = new DistanceFinder(myX,myY, SENSOR_LENGTH, delayedXDirection(-45.0),delayedYDirection(-45.0));
//    myDistanceFinders.add(myForwardFinder);
//    myDistanceFinders.add(myRightFinder);
//    myDistanceFinders.add(myForwardRightFinder);
//    myDistanceFinders.add(myLeftFinder);
//    myDistanceFinders.add(myForwardLeftFinder);
//    showingFinders = false;
//    myDistanceFinderRoot = new Group();
//    for (DistanceFinder finder : myDistanceFinders) {
//      myDistanceFinderRoot.getChildren().add(finder);
//    }
//    myListeners = new ArrayList<>();
//    myCollisionListeners = new ArrayList<>();
//    setPosition(x,y);
//  }
//
//  public void showFinders(boolean show) {
//    if (show && !showingFinders) {
//      getChildren().add(myDistanceFinderRoot);
//    }
//    if (!show && showingFinders) {
//      getChildren().remove(myDistanceFinderRoot);
//    }
//    showingFinders = show;
//  }
//
//  public void pressPedal(double pressAmount) {
//    myPedalPress = Math.max(0.0,Math.min(pressAmount,1.0));
//  }
//
//  public void turnWheel(double turnDegrees) {
//    myWheelRotation += turnDegrees;
//    myWheelRotation = Math.max(-MAX_WHEEL_ROTATION,Math.min(myWheelRotation,MAX_WHEEL_ROTATION));
//  }
//
//  /**
//   *
//   * @param deltaTime The number of seconds elapsed since the last update.
//   */
//  public void updateSelf(double deltaTime, List<CollisionEntity> obstacles) {
//    double wheelRadians = degreesToRadians(myWheelRotation + myRotation);
//    double xComponent = Math.cos(wheelRadians);
//    double yComponent = Math.sin(wheelRadians);
//    double alignment = (dotProduct2(myXDirection, myYDirection,xComponent,yComponent));
//
//    double accelerationForward = (alignment * (myPedalPress * MAX_ACCELERATION)) - (mySpeed * FRICTION_FACTOR);
//    mySpeed += accelerationForward;
//
//    //Our ACCELERATION, not SPEED, is modified by alignment.
//    double xChange = mySpeed * deltaTime * myXDirection;
//    double yChange = mySpeed * deltaTime * myYDirection;
//    setPosition(myX + xChange, myY + yChange);
//    double turnCircumference = (CAR_RADIUS *2*Math.PI);
//    double angleChange = (deltaTime * mySpeed) / turnCircumference * myWheelRotation;
//    setRotation(myRotation + angleChange);
//    findDistances(obstacles);
//  }
//
//  @Override
//  public void handleEntityCollision(CollisionEntity otherEntity) {
//    if (otherEntity.getType().equals("Wall") || otherEntity.getType().equals("Car")) {
//      for (CollisionEntityListener listener: myCollisionListeners) {
//        listener.reactToRemoval(this);
//      }
//      getChildren().clear();
//    }
//  }
//
//  @Override
//  public String getType() {
//    return "Car";
//  }
//
//  @Override
//  public double getCollisionWidth() {
//    return CAR_LENGTH;
//  }
//
//  @Override
//  public double getCollisionHeight() {
//    return CAR_WIDTH;
//  }
//
//  @Override
//  public double getCollisionX() {
//    return myX;
//  }
//
//  @Override
//  public double getCollisionY() {
//    return myY;
//  }
//
//  public void givePoints(double points) {
//    for (CarListener listener : myListeners) {
//      listener.reactToPointGain(points);
//    }
//  }
//
//  public void subscribe(CarListener listener) {
//    myListeners.add(listener);
//  }
//
//
//  public void setPosition(double xPos, double yPos) {
//    myX = xPos;
//    myY = yPos;
//    double middleX = myX - (CAR_LENGTH * 0.5);
//    double middleY = myY - (CAR_WIDTH * 0.5);
//    myCarImage.setX(middleX);
//    myCarImage.setY(middleY);
//    for (DistanceFinder finder : myDistanceFinders) {
//      finder.setPosition(myX,myY);
//    }
//  }
//
//  public double getPedalPress() {
//    return myPedalPress;
//  }
//
//  public double getWheelTurn() {
//    return myWheelRotation;
//  }
//
//  public double getForwardDistance() {
//    return myForwardFinder.getDistance();
//  }
//
//  public double getLeftDistance() {
//    return myLeftFinder.getDistance();
//  }
//
//  public double getForwardLeftDistance() {
//    return myForwardLeftFinder.getDistance();
//  }
//
//  public double getRightDistance() {
//    return myRightFinder.getDistance();
//  }
//
//  public double getForwardRightDistance() {
//    return myForwardRightFinder.getDistance();
//  }
//
//  public double getSpeed() {
//    return mySpeed;
//  }
//
//  public double getXPos() {
//    return myX;
//  }
//
//  public double getYPos() {
//    return myY;
//  }
//
//  public void setRotation(double degreeAmount) {
//    myRotation = degreeAmount;
//    myXDirection = Math.cos(degreesToRadians(myRotation));
//    myYDirection = Math.sin(degreesToRadians(myRotation));
//    myCarImage.setRotate(myRotation);
//    myForwardFinder.setDirection(myXDirection,myYDirection);
//    myRightFinder.setDirection(delayedXDirection(90.0),delayedYDirection(90.0));
//    myForwardRightFinder.setDirection(delayedXDirection(45.0),delayedYDirection(45.0));
//    myLeftFinder.setDirection(delayedXDirection(-90.0),delayedYDirection(-90.0));
//    myForwardLeftFinder.setDirection(delayedXDirection(-45.0),delayedYDirection(-45.0));
//  }
//
//  @Override
//  public boolean pointInBounds(double xPos, double yPos) {
//    //TODO: Make this depend on rotation.
//    return ((xPos > myX) && (xPos < myX + CAR_LENGTH) && (yPos > myY) && (yPos < myY + CAR_LENGTH));
//  }
//
//  @Override
//  public void subscribe(CollisionEntityListener subscriber) {
//    myCollisionListeners.add(subscriber);
//  }
//
//  private double dotProduct2(double x1, double x2, double y1, double y2) {
//    return (x1 * y1) + (x2 * y2);
//  }
//
//  private double degreesToRadians(double angleDegrees) {
//    return (angleDegrees / 360.0) * 2 * Math.PI;
//  }
//
//  private void findDistances(List<CollisionEntity> obstacles) {
//    List<CollisionEntity> otherObstacles = new ArrayList<>(obstacles);
//    otherObstacles.remove(this);
//    for (DistanceFinder finder : myDistanceFinders) {
//      finder.calcDistance(0,0,1200,1000,otherObstacles);
//    }
//  }
//
//  private double delayedXDirection(double delayAngleDegrees) {
//    return Math.cos(degreesToRadians(myRotation) - degreesToRadians(delayAngleDegrees));
//  }
//
//  private double delayedYDirection(double delayAngleDegrees) {
//    return Math.sin(degreesToRadians(myRotation) - degreesToRadians(delayAngleDegrees));
//  }
//
//}
