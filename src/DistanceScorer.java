public class DistanceScorer implements CarScorer {
  @Override
  public double scoreCar(CarVisualizer car) {
    return car.getSpeed();
  }
}
