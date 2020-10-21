package model.entity;

import model.CollisionEntity;
import model.CollisionEntityListener;
import model.DistanceFinder;

import java.util.ArrayList;
import java.util.List;

public class EvolutionCar implements Car {
  public static final double MAX_WHEEL_ROTATION = 40;
  public static final double FRICTION_FACTOR = 0.06;
  public static final double MAX_ACCELERATION = 20;
  public static final double CAR_RADIUS = 10.0;
  public static final int SENSOR_LENGTH = 300;
  public static final int BETWEEN_AXLE_LENGTH = 50;
  public static final int AXLE_WIDTH = 25;
  public static final double TIRE_FRICTION_FACTOR = 0.1;
  private static final double BRAKE_STRENGTH = 0.05;
  private double myX;
  private double myY;
  private double myFrontX;
  private double myFrontY;
  private double myBackX;
  private double myBackY;
  private double myRotation;
  private double myWheelRotation;
  private double myPedalPress;
  private double myBrakesPress;
  private double myXDirection;
  private double myYDirection;
  private double mySpeed;
  private double myXVelocity;
  private double myYVelocity;
  private double myBackXVelocity;
  private double myBackYVelocity;
  private List<DistanceFinder> myDistanceFinders;
  private DistanceFinder myForwardFinder;
  private DistanceFinder myLeftFinder;
  private DistanceFinder myForwardLeftFinder;
  private DistanceFinder myRightFinder;
  private DistanceFinder myForwardRightFinder;
  private List<CarListener> myListeners;
  private List<CollisionEntityListener> myCollisionListeners;

  public EvolutionCar(double x, double y) {
    myDistanceFinders = new ArrayList<>();
    myForwardFinder = (new DistanceFinder(myX,myY, SENSOR_LENGTH,myXDirection,myYDirection));
    myLeftFinder = new DistanceFinder(myX,myY, SENSOR_LENGTH, delayedXDirection(90.0),delayedYDirection(90.0));
    myForwardLeftFinder = new DistanceFinder(myX,myY, SENSOR_LENGTH, delayedXDirection(45.0),delayedYDirection(45.0));
    myRightFinder = new DistanceFinder(myX,myY, SENSOR_LENGTH, delayedXDirection(-90.0),delayedYDirection(-90.0));
    myForwardRightFinder = new DistanceFinder(myX,myY, SENSOR_LENGTH, delayedXDirection(-45.0),delayedYDirection(-45.0));
    myDistanceFinders.add(myForwardFinder);
    myDistanceFinders.add(myRightFinder);
    myDistanceFinders.add(myForwardRightFinder);
    myDistanceFinders.add(myLeftFinder);
    myDistanceFinders.add(myForwardLeftFinder);
    myPedalPress = 0;
    myWheelRotation = 0;
    myBrakesPress = 0;
    myXVelocity = 0;
    myYVelocity = 0;
    myBackXVelocity = 0;
    myBackYVelocity = 0;
    myBackX = x;
    myBackY = y;
    myFrontX = myBackX + (Math.cos(myRotation) * BETWEEN_AXLE_LENGTH);
    myFrontY = myBackY + (Math.sin(myRotation) * BETWEEN_AXLE_LENGTH);
    mySpeed = Math.sqrt((myXVelocity * myXVelocity) + (myYVelocity * myYVelocity));
    for (DistanceFinder finder : myDistanceFinders) {
    }
    myListeners = new ArrayList<>();
    myCollisionListeners = new ArrayList<>();
    setPosition(x,y);
  }

  public void pressPedal(double pressAmount) {
    myPedalPress = Math.max(0.0,Math.min(pressAmount,1.0));
  }

  public void turnWheel(double turnDegrees) {
    myWheelRotation += turnDegrees;
    myWheelRotation = Math.max(-MAX_WHEEL_ROTATION,Math.min(myWheelRotation,MAX_WHEEL_ROTATION));
  }

  @Override
  public void pressBrakes(double pressAmount) {
    myBrakesPress = Math.max(0.0,Math.min(pressAmount,1.0));
  }

  /**
   *
   * @param deltaTime The number of seconds elapsed since the last update.
   */
  public void updateSelf(double deltaTime, List<CollisionEntity> obstacles) {

    double backWheelDirection = degreesToRadians(myRotation);
    double frontWheelDirection = degreesToRadians(myWheelRotation + myRotation);

    //Projection onto my direction of my velocity vector
    double frontWheelDirX = Math.cos(frontWheelDirection);
    double frontWheelDirY = Math.sin(frontWheelDirection);
    double velocityProjectionDot = dotProduct2(myXVelocity,myYVelocity,frontWheelDirX,frontWheelDirY);
    double velocityProjectionX = velocityProjectionDot * frontWheelDirX;
    double velocityProjectionY = velocityProjectionDot * frontWheelDirY;
    double normalVelocityX = myXVelocity - velocityProjectionX;
    double normalVelocityY = myYVelocity - velocityProjectionY;
    double decelerationX = normalVelocityX * -TIRE_FRICTION_FACTOR;
    double decelerationY = normalVelocityY * -TIRE_FRICTION_FACTOR;
    //TODO: What if brake strength is strong enough to send the car backwards?
    double frictionDecelerationX = -myXVelocity * (FRICTION_FACTOR + (myBrakesPress * BRAKE_STRENGTH));
    double frictionDecelerationY = -myYVelocity * (FRICTION_FACTOR + (myBrakesPress * BRAKE_STRENGTH));


    //Have the halves of the vehicle pulled toward each other by a spring/bar
    //front - back
    double backToFrontX = myFrontX - myBackX;
    double backToFrontY = myFrontY - myBackY;
    //normalize it, multiply it by the car length, and subtract it from front.
    double backToFrontDistance = Math.sqrt(Math.pow(backToFrontX,2)+Math.pow(backToFrontY,2));
    double backToFrontDiff = backToFrontDistance - BETWEEN_AXLE_LENGTH;

    double backToFrontNormalizedX = (backToFrontX / backToFrontDistance);
    double backToFrontNormalizedY = (backToFrontY / backToFrontDistance);
    double backToFrontAngle = Math.atan2(backToFrontNormalizedY,backToFrontNormalizedX);
    double forceMagnitude = backToFrontDiff * 2.0;
    double pullingForceX = forceMagnitude * backToFrontNormalizedX;
    double pullingForceY = forceMagnitude * backToFrontNormalizedY;

    double backXFriction = myBackXVelocity * -FRICTION_FACTOR;
    double backYFriction = myBackYVelocity * -FRICTION_FACTOR;

    //TODO: Add a pulling force that pulls the front to the back also.
    myXVelocity += decelerationX + frictionDecelerationX + (-0.5 * pullingForceX);
    myYVelocity += decelerationY + frictionDecelerationY + (-0.5 * pullingForceY);

    double accelerationX = Math.cos(backWheelDirection) * MAX_ACCELERATION * deltaTime * myPedalPress;
    double accelerationY = Math.sin(backWheelDirection) * MAX_ACCELERATION * deltaTime * myPedalPress;
    myBackXVelocity += accelerationX + (0.5 * pullingForceX) + backXFriction;
    myBackYVelocity += accelerationY + (0.5 * pullingForceY) + backYFriction;
    myBackX += myBackXVelocity;
    myBackY += myBackYVelocity;
    myFrontX += myXVelocity;
    myFrontY += myYVelocity;
    backToFrontX = (backToFrontX / backToFrontDistance) * BETWEEN_AXLE_LENGTH;
    backToFrontY = (backToFrontY / backToFrontDistance) * BETWEEN_AXLE_LENGTH;
//    myBackX = myFrontX - backToFrontX;
//    myBackY = myFrontY - backToFrontY;

    setPosition(myBackX,myBackY);
    setRotation((backToFrontAngle / (2 * Math.PI)) * 360);

//    double wheelRadians = degreesToRadians(myWheelRotation + myRotation);
//    double xComponent = Math.cos(wheelRadians);
//    double yComponent = Math.sin(wheelRadians);
//    double alignment = (dotProduct2(myXDirection, myYDirection,xComponent,yComponent));
//    alignment *= 1 - myBrakesPress;
//    System.out.println("alignment = " + alignment);
//    System.out.println("myXVelocity = " + myXVelocity);
//    System.out.println("myYVelocity = " + myYVelocity);
//    System.out.println("mySpeed = " + mySpeed);
//    double xVelDirection = myXVelocity / mySpeed;
//    double yVelDirection = myYVelocity / mySpeed;
//    if (mySpeed == 0) {
//      xVelDirection = 0; yVelDirection = 0;
//    }
//    System.out.println("xVelDirection = " + xVelDirection);
//    System.out.println("yVelDirection = " + yVelDirection);
//    System.out.println("xComponent = " + xComponent);
//    System.out.println("yComponent = " + yComponent);
//    double velocityAlignment = dotProduct2(xVelDirection,yVelDirection,xComponent,yComponent);
//    System.out.println("velocityAlignment = " + velocityAlignment);
//
//    double accelerationForward = (alignment * (myPedalPress * MAX_ACCELERATION)) - (mySpeed * FRICTION_FACTOR);
//    //mySpeed += accelerationForward;
//    //The velocity should be dampened based on the alignment to the wheels, since wheels are good friction surfaces
//    // when you aren't moving in their direction of rotation
//    double xAcceleration = (myPedalPress * MAX_ACCELERATION) * xComponent;
//    double xDeceleration = (myXVelocity * FRICTION_FACTOR) + (myXVelocity * TIRE_FRICTION_FACTOR * (1-velocityAlignment));
//    double yAcceleration = (myPedalPress * MAX_ACCELERATION) * yComponent;
//    double yDeceleration = (myYVelocity * FRICTION_FACTOR) + (myYVelocity * TIRE_FRICTION_FACTOR * (1-velocityAlignment));
//    myXVelocity += xAcceleration - xDeceleration;
//    myYVelocity += yAcceleration - yDeceleration;
//    mySpeed = Math.sqrt((myXVelocity * myXVelocity) + (myYVelocity  * myYVelocity));
//
//    //Our ACCELERATION, not SPEED, is modified by alignment.
//    //double xChange = mySpeed * deltaTime * myXDirection;
//    //double yChange = mySpeed * deltaTime * myYDirection;
//    double xChange = myXVelocity * deltaTime;
//    double yChange = myYVelocity * deltaTime;
//    setPosition(myX + xChange, myY + yChange);
//    double turnCircumference = (CAR_RADIUS *2*Math.PI);
//    double angleChange = (deltaTime * mySpeed) / turnCircumference * myWheelRotation;
//    setRotation(myRotation + angleChange);
    findDistances(obstacles);
  }

  @Override
  public void handleEntityCollision(CollisionEntity otherEntity) {
    if (otherEntity.getType().equals("Wall") || otherEntity.getType().equals("asdf")) {
      destroy();
    }
  }

  @Override
  public String getType() {
    return "Car";
  }

  @Override
  public double getCollisionWidth() {
    return BETWEEN_AXLE_LENGTH;
  }

  @Override
  public double getCollisionHeight() {
    return AXLE_WIDTH;
  }

  @Override
  public double getCollisionX() {
    return myX;
  }

  @Override
  public double getCollisionY() {
    return myY;
  }

  public void givePoints(double points) {
    for (CarListener listener : myListeners) {
      listener.reactToPointGain(points);
    }
  }

  public void subscribe(CarListener listener) {
    myListeners.add(listener);
  }


  public void setPosition(double xPos, double yPos) {
    myX = xPos;
    myY = yPos;
    myBackX = xPos;
    myBackY = yPos;
    for (DistanceFinder finder : myDistanceFinders) {
      finder.setPosition(myX,myY);
    }
    for (CarListener listener : myListeners) {
      listener.reactToPositionChange(myX,myY);
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

  public double getForwardLeftDistance() {
    return myForwardLeftFinder.getDistance();
  }

  public double getRightDistance() {
    return myRightFinder.getDistance();
  }

  public double getForwardRightDistance() {
    return myForwardRightFinder.getDistance();
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

  @Override
  public void destroy() {
    for (CollisionEntityListener listener: myCollisionListeners) {
      listener.reactToRemoval(this);
    }
    for (CarListener listener : myListeners) {
      listener.reactToRemoval(this);
    }
  }

  public void setRotation(double degreeAmount) {
    myRotation = degreeAmount;
    myXDirection = Math.cos(degreesToRadians(myRotation));
    myYDirection = Math.sin(degreesToRadians(myRotation));
    myForwardFinder.setDirection(myXDirection,myYDirection);
    myRightFinder.setDirection(delayedXDirection(90.0),delayedYDirection(90.0));
    myForwardRightFinder.setDirection(delayedXDirection(45.0),delayedYDirection(45.0));
    myLeftFinder.setDirection(delayedXDirection(-90.0),delayedYDirection(-90.0));
    myForwardLeftFinder.setDirection(delayedXDirection(-45.0),delayedYDirection(-45.0));
    for (CarListener listener : myListeners) {
      listener.reactToDirectionChange(myRotation);
    }
  }


  @Override
  public boolean pointInBounds(double xPos, double yPos) {
    //TODO: Make this depend on rotation.
    return ((xPos > myX) && (xPos < myX + BETWEEN_AXLE_LENGTH) && (yPos > myY) && (yPos < myY + BETWEEN_AXLE_LENGTH));
  }

  @Override
  public void subscribe(CollisionEntityListener subscriber) {
    myCollisionListeners.add(subscriber);
  }

  private double dotProduct2(double x1, double x2, double y1, double y2) {
    return (x1 * y1) + (x2 * y2);
  }

  private double degreesToRadians(double angleDegrees) {
    return (angleDegrees / 360.0) * 2 * Math.PI;
  }

  private void findDistances(List<CollisionEntity> obstacles) {
    List<CollisionEntity> otherObstacles = new ArrayList<>(obstacles);
    otherObstacles.remove(this);
    for (DistanceFinder finder : myDistanceFinders) {
      finder.calcDistance(0,0,1200,1000,otherObstacles,List.of("Wall"));
    }
  }

  private double delayedXDirection(double delayAngleDegrees) {
    return Math.cos(degreesToRadians(myRotation) - degreesToRadians(delayAngleDegrees));
  }

  private double delayedYDirection(double delayAngleDegrees) {
    return Math.sin(degreesToRadians(myRotation) - degreesToRadians(delayAngleDegrees));
  }

  @Override
  public double getBackXPos() {
    return myBackX;
  }

  @Override
  public double getBackYPos() {
    return myBackY;
  }

  @Override
  public double getFrontXPos() {
    return myFrontX;
  }

  @Override
  public double getFrontYPos() {
    return myFrontY;
  }
}

