package model.entity;

import model.CollisionEntity;

import java.util.List;

public interface Car extends CollisionEntity {

  public void pressPedal(double pressAmount);

  public void turnWheel(double turnDegrees);

  public void pressBrakes(double pressAmount);

  /**
   *
   * @param deltaTime The number of seconds elapsed since the last update.
   */
  public void updateSelf(double deltaTime, List<CollisionEntity> obstacles);

  public void givePoints(double points);

  public void subscribe(CarListener listener);

  public void setPosition(double xPos, double yPos);

  public double getPedalPress();

  public double getWheelTurn();

  public double getForwardDistance();

  public double getLeftDistance();

  public double getForwardLeftDistance();

  public double getRightDistance();

  public double getForwardRightDistance();

  public double getSpeed();

  public double getXPos();

  public double getYPos();

  public double getBackXPos();

  public double getBackYPos();

  public double getFrontXPos();

  public double getFrontYPos();

  public void destroy();

  public double getLengthBetweenAxles();

  public double getAxleWidth();
}
