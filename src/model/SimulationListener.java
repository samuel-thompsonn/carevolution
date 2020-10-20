package model;

import model.entity.WallRegion;

public interface SimulationListener {
  public void reactToNewIteration();
  //TODO: Yes, this is an interface relying on an implementation.
  public void reactToNewWall(WallRegion wall);
}
