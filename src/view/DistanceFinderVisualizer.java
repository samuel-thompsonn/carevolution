package view;

import javafx.scene.Parent;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import model.CollisionEntity;

import java.util.List;

public class DistanceFinderVisualizer extends Parent {
  private Line myLine;
  private Text myDistanceDisplay;
  private double myX;
  private double myY;
  private double myXDirection;
  private double myYDirection;
  private double myLength;
  private double myDistance;

  public DistanceFinderVisualizer(double x, double y, double length, double xDir, double yDir) {
    myLine = new Line();
    getChildren().add(myLine);
    myDistance = length;
    myDistanceDisplay = new Text("0");
    myDistanceDisplay.setX(myX);
    myDistanceDisplay.setY(myY + 50);
    getChildren().add(myDistanceDisplay);
    setPosition(x,y);
    setLength(length);
    setDirection(xDir,yDir);
  }

  public void setPosition(double x, double y) {
    myX = x;
    myY = y;
    myLine.setStartX(x);
    myLine.setStartY(y);
    adjustTextPos();
    adjustTextPos();
    refreshLine();
  }

  private void adjustTextPos() {
    myDistanceDisplay.setX(myX + (myXDirection * myLength * 0.5));
    myDistanceDisplay.setY(myY + (myYDirection * myLength * 0.5));
  }

  public void setDirection(double x, double y) {
    double magnitude = Math.sqrt((x*x)+(y*y));
    myXDirection = x / magnitude;
    myYDirection = y / magnitude;
    adjustTextPos();
    refreshLine();
  }

  public void calcDistance(double minX, double minY, double maxX, double maxY, List<CollisionEntity> obstacles) {
    myDistance = findDistance(0,myLength,minX,minY,maxX,maxY, obstacles);
    myDistanceDisplay.setText(""+myDistance);
  }

  public double getDirectionX() {
    return myXDirection;
  }

  public double getDirectionY() {
    return myYDirection;
  }

  private double findDistance(double minDistance, double maxDistance, double minX, double minY, double maxX, double maxY, List<CollisionEntity> obstacles) {
    double currentDistance = (minDistance + maxDistance) / 2.0;

    if (maxDistance - minDistance < 1.0) {
      return currentDistance;
    }

    double targetX = myX + (currentDistance * myXDirection);
    double targetY = myY + (currentDistance * myYDirection);

    //If we are colliding with an obstacle, we must go closer
    if (targetX < minX || targetX > maxX || targetY < minY || targetY > maxY) {
      return findDistance(minDistance, currentDistance, minX,minY,maxX,maxY, obstacles);
    }
    for (CollisionEntity obstacle: obstacles) {
      if (obstacle.pointInBounds(targetX, targetY)) {
        return findDistance(minDistance, currentDistance, minX,minY,maxX,maxY, obstacles);
      }
    }
    //If we are not colliding with an obstacle, we could go closer OR farther.
    //If there's an obstacle closer, return that one.
    double fartherDistance = findDistance(currentDistance,maxDistance,minX,minY,maxX,maxY,obstacles);
    double closerDistance = findDistance(minDistance, currentDistance, minX,minY,maxX,maxY, obstacles);
    if (closerDistance < currentDistance - 1) {
      return closerDistance;
    }
    return fartherDistance;
  }

  public void setLength(double length) {
    myLength = length;
    refreshLine();
  }

  public double getDistance() {
    return myDistance;
  }

  private void refreshLine() {
    myLine.setStartX(myX);
    myLine.setStartY(myY);
    myLine.setEndX(myX + (myDistance * myXDirection));
    myLine.setEndY(myY + (myDistance * myYDirection));
  }

}
