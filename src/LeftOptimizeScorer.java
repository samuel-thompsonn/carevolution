/**
 * Incentivizes cars to keep their left side close to an obstacle.
 */
public class LeftOptimizeScorer implements CarScorer {
  @Override
  public double scoreCar(CarVisualizer car) {
    return CarVisualizer.SENSOR_LENGTH - car.getLeftDistance();
  }
}
