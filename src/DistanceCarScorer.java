public class DistanceCarScorer implements CarScorer {
  @Override
  public double scoreCar(CarVisualizer car) {
    return car.getSpeed();
  }
}
