package model.entity;

import model.CollisionEntityListener;

public interface CarListener extends CollisionEntityListener {
  public void reactToPointGain(double pointGain);

  public void reactToPositionChange(double newX, double newY);

  public void reactToDirectionChange(double directionDegrees);
}
