package model;

import model.entity.Car;
import model.entity.WallRegion;

import java.util.List;

public interface CarSim {
  public List<Car> getCars();

  public List<WallRegion> getWalls();

  public void placeObstacle(double xPos, double yPos, double width, double height);

  public void clearObstacles();

  public void update(double elapsedSeconds);

  public void startNewRound();

  public void subscribe(SimulationListener listener);

  public void resetCars();
}
