public class StraightScorer implements CarScorer {
  @Override
  public double scoreCar(CarVisualizer car) {
    if (car.getSpeed() < 80 && car.getSpeed() > 70 && car.getWheelTurn() > -10 && car.getWheelTurn() < 10) {
      return 1;
    }
    return 0;
  }
}
