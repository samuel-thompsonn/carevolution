package model;

import model.CollisionEntity;

import java.lang.reflect.Array;
import java.util.Arrays;
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
    //myDistance = findDistance(0,myLength,minX,minY,maxX,maxY, obstacles, threats);
    myDistance = myLength;
    for (CollisionEntity obstacle: obstacles) {
      if (!obstacleIsThreat(obstacle,threats)) {
        continue;
      }
      double[] start = new double[]{
              myX,myY
      };
      double[] end = new double[]{
              myX + (myXDirection * myLength),
              myY + (myYDirection * myLength)
      };
      List<double[]> points = obstacle.getHitboxPoints();
      for (int i = 0; i < points.size(); i ++) {
        int next = i+1;
        if (next == points.size()) {
          next = 0;
        }
        if (linesIntersecting(start,end,points.get(i),points.get(next))) {
          double[] intersectPoint = getLineIntersection(start,end,points.get(i),points.get(next));
          double intersectDistance = Math.sqrt(Math.pow(intersectPoint[0]-myX,2)+Math.pow(intersectPoint[1]-myY,2));
          myDistance = Math.min(myDistance,intersectDistance);
        }
      }
    }
  }

  public double getDirectionX() {
    return myXDirection;
  }

  public double getDirectionY() {
    return myYDirection;
  }

  private double findDistance(double minDistance, double maxDistance, double minX, double minY, double maxX, double maxY, List<CollisionEntity> obstacles, List<String> threats) {
    return 0;
//    double currentDistance = (minDistance + maxDistance) / 2.0;
//
//    if (maxDistance - minDistance < 1.0) {
//      return currentDistance;
//    }
//
//    double targetX = myX + (currentDistance * myXDirection);
//    double targetY = myY + (currentDistance * myYDirection);
//
//    //If we are colliding with an obstacle, we must go closer
//    if (targetX < minX || targetX > maxX || targetY < minY || targetY > maxY) {
//      return findDistance(minDistance, currentDistance, minX,minY,maxX,maxY, obstacles, threats);
//    }
//    for (CollisionEntity obstacle: obstacles) {
//      if (obstacle.pointInBounds(targetX, targetY) && obstacleIsThreat(obstacle,threats)) {
//        return findDistance(minDistance, currentDistance, minX,minY,maxX,maxY, obstacles, threats);
//      }
//    }
//    //If we are not colliding with an obstacle, we could go closer OR farther.
//    //If there's an obstacle closer, return that one.
//    double fartherDistance = findDistance(currentDistance,maxDistance,minX,minY,maxX,maxY,obstacles, threats);
//    double closerDistance = findDistance(minDistance, currentDistance, minX,minY,maxX,maxY, obstacles, threats);
//    if (closerDistance < currentDistance - 1) {
//      return closerDistance;
//    }
//    return fartherDistance;
  }

  private boolean linesIntersecting(double[] a, double[] b, double[] c, double[] d) {
    double[] intersection = getLineIntersection(a,b,c,d);
    boolean intersectsABx = (intersection[0]>= Math.min(a[0],b[0]) && intersection[0] <= Math.max(a[0],b[0]));
    boolean intersectsCDx = (intersection[0] >= Math.min(c[0],d[0]) && intersection[0] <= Math.max(c[0],d[0]));
//    boolean intersectsABy = (intersection[1]>= Math.min(a[1],b[1]) && intersection[1] <= Math.max(a[1],b[1]));
    boolean intersectsCDy = (intersection[1] >= Math.min(c[1],d[1])-1 && intersection[1] <= Math.max(c[1],d[1])+1);
    return intersectsABx && intersectsCDx && intersectsCDy;
  }

  private double[] getLineIntersection(double[] a, double[] b, double[] c, double[] d) {
    double slopeAB = (b[1]-a[1]) / (b[0]-a[0]);
    double slopeCD = (d[1]-c[1]) / (d[0]-c[0]);
    if (slopeAB == slopeCD) {
      return new double[]{-10,-10};
    }
    if (b[0]==a[0]) {
      double[] diffCD = new double[]{
              d[0]-c[0],
              d[1]-c[1]
      };
      double scalar = (a[0]-c[0])/diffCD[0];
      return new double[]{
              c[0] + (scalar * diffCD[0]),
              c[1] + (scalar * diffCD[1])
      };
    }
    if (d[0]==c[0]) {
      double[] diffAB = new double[]{
              b[0]-a[0],
              b[1]-a[1]
      };
      double scalar = (c[0]-a[0])/diffAB[0];
      return new double[]{
              a[0] + (scalar * diffAB[0]),
              a[1] + (scalar * diffAB[1])
      };
    }
    double intersectionX = ((-c[0] * slopeCD) + c[1] + (a[0]*slopeAB) - a[1]) / (slopeAB - slopeCD);
    double intersectionY = (slopeAB * (intersectionX - a[0])) + a[1];
    return new double[]{intersectionX,intersectionY};
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
