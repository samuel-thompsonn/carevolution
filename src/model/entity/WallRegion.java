package model.entity;

import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import model.CollisionEntity;
import model.CollisionEntityListener;

import java.util.List;


public class WallRegion extends Parent implements CollisionEntity {
  private Shape myRegion;
  private double myX;
  private double myY;
  private double myWidth;
  private double myHeight;

  public WallRegion(double xPos, double yPos, double width, double height) {
    myRegion = new Rectangle(xPos,yPos,width,height);
    myRegion.setFill(Color.GRAY);
    getChildren().add(myRegion);
    myX = xPos;
    myY = yPos;
    myWidth = width;
    myHeight = height;
  }

  public boolean pointInBounds(double xPos, double yPos) {
    return (xPos >= myX && xPos < myX + myWidth &&
            yPos >= myY && yPos < myY + myHeight);
  }

  @Override
  public void subscribe(CollisionEntityListener subscriber) {
    // do nothing, no information needs to be sent.
  }

  @Override
  public void updateSelf(double deltaTime, List<CollisionEntity> otherObstacles) {
    // has no behavior.
  }

  @Override
  public void handleEntityCollision(CollisionEntity otherEntity) {
    // has no behavior
  }

  @Override
  public String getType() {
    return "Wall";
  }

  @Override
  public double getCollisionWidth() {
    return myWidth;
  }

  @Override
  public double getCollisionHeight() {
    return myHeight;
  }

  @Override
  public double getCollisionX() {
    return myX;
  }

  @Override
  public double getCollisionY() {
    return myY;
  }
}
