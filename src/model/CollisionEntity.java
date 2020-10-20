package model;

import java.util.List;

public interface CollisionEntity {
  public boolean pointInBounds(double xPos, double yPos);
  public void subscribe(CollisionEntityListener subscriber);
  public void updateSelf(double deltaTime, List<CollisionEntity> obstacles);
  public void handleEntityCollision(CollisionEntity otherEntity);
  public String getType();
  public double getCollisionWidth();
  public double getCollisionHeight();
  public double getCollisionX();
  public double getCollisionY();
  }
