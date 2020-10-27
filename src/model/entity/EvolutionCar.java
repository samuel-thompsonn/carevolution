package model.entity;

import model.CollisionEntity;
import model.CollisionEntityListener;
import model.DistanceFinder;

import java.util.ArrayList;
import java.util.List;

public class EvolutionCar implements Car {
  public static final double MAX_WHEEL_ROTATION = 45;
  public static final double FRICTION_FACTOR = 0.01;
  public static final double MAX_ACCELERATION = 20;
  public static final double CAR_RADIUS = 10.0;
  public static final int SENSOR_LENGTH = 300;
  public static final int BETWEEN_AXLE_LENGTH = 25;
  public static final double AXLE_WIDTH = 12.5;
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
  private List<double[]> myHitboxPoints;
  private List<double[]> myRectBoundPoints;

  public EvolutionCar(double x, double y) {
    myHitboxPoints = new ArrayList<>();
    myDistanceFinders = new ArrayList<>();
    myRectBoundPoints = new ArrayList<>();
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

//    //Projection onto my direction of my velocity vector
    double normalVelocityX = getVectorNormalX(frontWheelDirection, myXVelocity, myYVelocity);
    double normalVelocityY = getVectorNormalY(frontWheelDirection, myXVelocity,myYVelocity);
    double wheelVelocityX = myXVelocity - normalVelocityX;
    double wheelVelocityY = myYVelocity - normalVelocityY;
    double decelerationX = (normalVelocityX + (wheelVelocityX * myBrakesPress)) * -TIRE_FRICTION_FACTOR;
    double decelerationY = (normalVelocityY + (wheelVelocityY * myBrakesPress)) * -TIRE_FRICTION_FACTOR;
    //TODO: What if brake strength is strong enough to send the car backwards?
    double frictionDecelerationX = -myXVelocity * (FRICTION_FACTOR);
    double frictionDecelerationY = -myYVelocity * (FRICTION_FACTOR);

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

    double backVelocityNormalX = getVectorNormalX(backWheelDirection,myBackXVelocity,myBackYVelocity);
    double backVelocityNormalY = getVectorNormalY(backWheelDirection,myBackXVelocity,myBackYVelocity);
    double backVelocityWheelX = myBackXVelocity - backVelocityNormalX;
    double backVelocityWheelY = myBackYVelocity - backVelocityNormalY;
    double backDecelerationX = (backVelocityNormalX + (myBrakesPress * backVelocityWheelX)) * -TIRE_FRICTION_FACTOR;
    double backDecelerationY = (backVelocityNormalY + (myBrakesPress * backVelocityWheelY)) * -TIRE_FRICTION_FACTOR;

    myXVelocity += decelerationX + frictionDecelerationX + (-0.5 * pullingForceX);
    myYVelocity += decelerationY + frictionDecelerationY + (-0.5 * pullingForceY);

    double accelerationX = Math.cos(backWheelDirection) * MAX_ACCELERATION * deltaTime * myPedalPress;
    double accelerationY = Math.sin(backWheelDirection) * MAX_ACCELERATION * deltaTime * myPedalPress;
    myBackXVelocity += accelerationX + (0.5 * pullingForceX) + backXFriction + backDecelerationX;
    myBackYVelocity += accelerationY + (0.5 * pullingForceY) + backYFriction + backDecelerationY;
    myBackX += myBackXVelocity;
    myBackY += myBackYVelocity;
    myFrontX += myXVelocity;
    myFrontY += myYVelocity;
    mySpeed = Math.sqrt(Math.pow(myXVelocity,2) + Math.pow(myYVelocity,2));

    setPosition(myBackX,myBackY);
    setRotation((backToFrontAngle / (2 * Math.PI)) * 360);

    findDistances(obstacles);
  }

  private double getVectorNormalX(double directionRadians, double vectorX, double vectorY) {
    double directionX = Math.cos(directionRadians);
    double directionY = Math.sin(directionRadians);
    double projectionDot = dotProduct2(vectorX,vectorY,directionX,directionY);
    double projectionX = projectionDot * directionX;
    return vectorX - projectionX;
  }

  private double getVectorNormalY(double directionRadians, double vectorX, double vectorY) {
    double directionX = Math.cos(directionRadians);
    double directionY = Math.sin(directionRadians);
    double projectionDot = dotProduct2(vectorX,vectorY,directionX,directionY);
    double projectionY = projectionDot * directionY;
    return vectorY - projectionY;
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

  @Override
  public List<double[]> getHitboxPoints() {
    return myHitboxPoints;
  }

  @Override
  public List<double[]> getHitboxRectPoints() {
    return myRectBoundPoints;
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
      finder.setPosition(myFrontX,myFrontY);
    }
    for (CarListener listener : myListeners) {
      listener.reactToPositionChange(myX,myY);
    }
    myHitboxPoints = calcHitboxPoints();
    myRectBoundPoints = calcRectBoundPoints();
  }

  private List<double[]> calcHitboxPoints() {
    double rotationRadians = degreesToRadians(myRotation);
    double rotationRadiansNormal = degreesToRadians(myRotation + 90);
    double offsetForwardX = Math.cos(rotationRadians) * AXLE_WIDTH * 0.5;
    double offsetForwardY = Math.sin(rotationRadians) * AXLE_WIDTH * 0.5;
    double offsetLeftX = Math.cos(rotationRadiansNormal) * AXLE_WIDTH * 0.5;
    double offsetLeftY = Math.sin(rotationRadiansNormal) * AXLE_WIDTH * 0.5;
    double[] firstPoint = {
            myFrontX + offsetLeftX + offsetForwardX,
            myFrontY + offsetLeftY + offsetForwardY
    };
    double[] secondPoint = {
            myFrontX - offsetLeftX + offsetForwardX,
            myFrontY - offsetLeftY + offsetForwardY
    };
    double[] thirdPoint = {
            myBackX - offsetLeftX - offsetForwardX,
            myBackY - offsetLeftY - offsetForwardY
    };
    double[] fourthPoint = {
            myBackX + offsetLeftX - offsetForwardX,
            myBackY + offsetLeftY - offsetForwardY
    };
    return List.of(firstPoint,secondPoint,thirdPoint,fourthPoint);
  }

  private List<double[]> calcRectBoundPoints() {
    List<double[]> points = myHitboxPoints;
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
    return Math.sqrt(Math.pow(myBackXVelocity,2)+Math.pow(myBackYVelocity,2));
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

  @Override
  public double getLengthBetweenAxles() {
    return BETWEEN_AXLE_LENGTH;
  }

  @Override
  public double getAxleWidth() {
    return AXLE_WIDTH;
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

