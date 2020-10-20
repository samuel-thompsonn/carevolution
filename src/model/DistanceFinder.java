package model;

import model.CollisionEntity;

import java.util.List;

public class DistanceFinder {
  private double myX;
  private double myY;
  private double myXDirection;
  private double myYDirection;
  private double myLength;
  private double myDistance;

  public DistanceFinder(double x, double y, double length, double xDir, double yDir) {
    myDistance = length;
    setPosition(x,y);
    setLength(length);
    setDirection(xDir,yDir);
  }

  public void setPosition(double x, double y) {
    myX = x;
    myY = y;
  }

  public void setDirection(double x, double y) {
    double magnitude = Math.sqrt((x*x)+(y*y));
    myXDirection = x / magnitude;
    myYDirection = y / magnitude;
  }

  public void calcDistance(double minX, double minY, double maxX, double maxY, List<CollisionEntity> obstacles, List<String> threats) {
    myDistance = findDistance(0,myLength,minX,minY,maxX,maxY, obstacles, threats);
  }

  public double getDirectionX() {
    return myXDirection;
  }

  public double getDirectionY() {
    return myYDirection;
  }

  private double findDistance(double minDistance, double maxDistance, double minX, double minY, double maxX, double maxY, List<CollisionEntity> obstacles, List<String> threats) {
    double currentDistance = (minDistance + maxDistance) / 2.0;

    if (maxDistance - minDistance < 1.0) {
      return currentDistance;
    }

    double targetX = myX + (currentDistance * myXDirection);
    double targetY = myY + (currentDistance * myYDirection);

    //If we are colliding with an obstacle, we must go closer
    if (targetX < minX || targetX > maxX || targetY < minY || targetY > maxY) {
      return findDistance(minDistance, currentDistance, minX,minY,maxX,maxY, obstacles, threats);
    }
    for (CollisionEntity obstacle: obstacles) {
      if (obstacle.pointInBounds(targetX, targetY) && obstacleIsThreat(obstacle,threats)) {
        return findDistance(minDistance, currentDistance, minX,minY,maxX,maxY, obstacles, threats);
      }
    }
    //If we are not colliding with an obstacle, we could go closer OR farther.
    //If there's an obstacle closer, return that one.
    double fartherDistance = findDistance(currentDistance,maxDistance,minX,minY,maxX,maxY,obstacles, threats);
    double closerDistance = findDistance(minDistance, currentDistance, minX,minY,maxX,maxY, obstacles, threats);
    if (closerDistance < currentDistance - 1) {
      return closerDistance;
    }
    return fartherDistance;
  }

  private boolean obstacleIsThreat(CollisionEntity obstacle, List<String> threats) {
    for (String threatName : threats) {
      if (obstacle.getType().equals(threatName)) {
        return true;
      }
    }
    return false;
  }

  public void setLength(double length) {
    myLength = length;
  }

  public double getDistance() {
    return myDistance;
  }

}
