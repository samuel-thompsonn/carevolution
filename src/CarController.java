public interface CarController extends CarListener {
  public int compareScores(CarController other);
  public CarController produceOffspring(CarVisualizer car); //TODO: Interfaces shall not rely on implementations.
  public void addToScore();
  public void manipulateCar(double elapsedTime);
  public double getScore();
}
