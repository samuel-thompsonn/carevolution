package model.carscorer;

import model.entity.Car;

/**
 * Incentivizes cars to keep their left side close to an obstacle.
 */
public class LeftOptimizeScorer implements CarScorer {
  /**
   *
   * @param car
   * @return Always returns 0. To be fixed.
   */
  @Override
  public double scoreCar(Car car) {
    return 0;
  }
}
