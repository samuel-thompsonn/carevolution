public class SigmoidBehavior implements NeuronBehavior {
  @Override
  public double applyNeuronFunction(double inValue) {
    return 1.0 / (1+Math.exp(-inValue));
  }
}
